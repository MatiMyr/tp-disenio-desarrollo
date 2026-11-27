'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';

import Boton from '@/components/Boton';
import { CampoArea, CampoSelect, CampoTexto } from '@/components/Campo';
import Encabezado from '@/components/Encabezado';
import Mensaje, { ListaErrores } from '@/components/Mensaje';
import Modal from '@/components/Modal';
import { useConsultor } from '@/components/SesionConsultor';
import { api, mensajesDeError } from '@/lib/api';
import { iniciales } from '@/lib/sesion';

type CompetenciaDTO = { codigo: string; nombre: string };
type FactorDTO = { codigo: string; nombre: string; nombreCompetencia: string };
type PlantillaDTO = { idPlantilla: number; nombre: string; descripcion: string };
type PlantillaDetalleDTO = PlantillaDTO & { items: { orden: number; descripcion: string }[] };

type Opcion = { orden: string; descripcion: string; ponderacion: string };

const LARGO_NOMBRE = 40;
const LARGO_ENUNCIADO = 80;
const LARGO_DESCRIPCION = 100;
const LARGO_DESCRIPCION_OPCION = 50;
const MAXIMO_OPCIONES = 10;

function opcionVacia(orden: number): Opcion {
  return { orden: String(orden), descripcion: '', ponderacion: '' };
}

function aNumero(valor: string): number | null {
  const limpio = valor.replace(',', '.').trim();
  if (limpio === '') {
    return null;
  }
  const numero = Number(limpio);
  return Number.isNaN(numero) ? null : numero;
}

/** CU021: alta de pregunta con su grilla editable de opciones de respuesta. */
export default function PantallaAltaPregunta() {
  const router = useRouter();
  const usuario = useConsultor();

  const [competencias, setCompetencias] = useState<CompetenciaDTO[]>([]);
  const [factores, setFactores] = useState<FactorDTO[]>([]);
  const [plantillas, setPlantillas] = useState<PlantillaDTO[]>([]);

  const [competencia, setCompetencia] = useState('');
  const [factor, setFactor] = useState('');
  const [nombre, setNombre] = useState('');
  const [enunciado, setEnunciado] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [plantilla, setPlantilla] = useState('');
  const [opciones, setOpciones] = useState<Opcion[]>([opcionVacia(1), opcionVacia(2)]);

  const [errores, setErrores] = useState<string[]>([]);
  const [guardando, setGuardando] = useState(false);
  const [modal, setModal] = useState<'guardada' | 'cancelar' | null>(null);
  const [guardada, setGuardada] = useState('');
  const [tocado, setTocado] = useState(false);

  useEffect(() => {
    api.get<CompetenciaDTO[]>('/preguntas/competencias').then(setCompetencias).catch(() => undefined);
    api.get<PlantillaDTO[]>('/plantillas').then(setPlantillas).catch(() => undefined);
  }, []);

  useEffect(() => {
    setFactor('');
    if (!competencia) {
      setFactores([]);
      return;
    }
    api
      .get<FactorDTO[]>(`/preguntas/competencias/${competencia}/factores`)
      .then(setFactores)
      .catch((e) => setErrores(mensajesDeError(e)));
  }, [competencia]);

  /** La plantilla solo precarga orden y descripción: la ponderación la carga el consultor. */
  async function elegirPlantilla(id: string) {
    setPlantilla(id);
    if (!id) {
      return;
    }
    try {
      const detalle = await api.get<PlantillaDetalleDTO>(`/plantillas/${id}`);
      setOpciones(
        detalle.items.map((i) => ({
          orden: String(i.orden),
          descripcion: i.descripcion,
          ponderacion: '',
        })),
      );
      setTocado(true);
    } catch (e) {
      setErrores(mensajesDeError(e));
    }
  }

  function cambiarOpcion(indice: number, campo: keyof Opcion, valor: string) {
    setOpciones((previas) =>
      previas.map((o, i) => (i === indice ? { ...o, [campo]: valor } : o)),
    );
    setTocado(true);
  }

  function agregarOpcion() {
    setOpciones((previas) => [...previas, opcionVacia(previas.length + 1)]);
    setTocado(true);
  }

  function quitarOpcion(indice: number) {
    setOpciones((previas) => previas.filter((_, i) => i !== indice));
    setTocado(true);
  }

  const suma = opciones.reduce((total, o) => total + (aNumero(o.ponderacion) ?? 0), 0);
  const sumaOk = Math.abs(suma - 10) < 0.001;

  async function aceptar() {
    setErrores([]);
    setGuardando(true);
    try {
      await api.post('/preguntas', {
        nombrePregunta: nombre,
        enunciado,
        descripcion,
        codigoCompetencia: competencia,
        codigoFactor: factor,
        opciones: opciones.map((o) => ({
          orden: aNumero(o.orden) ?? 0,
          descripcion: o.descripcion,
          ponderacion: aNumero(o.ponderacion),
        })),
      });
      setGuardada(nombre.trim());
      setModal('guardada');
    } catch (e) {
      setErrores(mensajesDeError(e));
    } finally {
      setGuardando(false);
    }
  }

  function limpiar() {
    setCompetencia('');
    setFactor('');
    setNombre('');
    setEnunciado('');
    setDescripcion('');
    setPlantilla('');
    setOpciones([opcionVacia(1), opcionVacia(2)]);
    setErrores([]);
    setModal(null);
    setTocado(false);
  }

  function cancelar() {
    if (tocado || nombre || enunciado) {
      setModal('cancelar');
    } else {
      router.push('/preguntas');
    }
  }

  if (!usuario) {
    return <div className="cargando">Cargando…</div>;
  }

  return (
    <div className="pantalla">
      <Encabezado
        migas={[
          { texto: 'Inicio', ruta: '/inicio' },
          { texto: 'Preguntas', ruta: '/preguntas' },
          { texto: 'Dar de Alta Pregunta' },
        ]}
        usuario={usuario}
        inicialesUsuario={iniciales(usuario)}
      />
      <div className="content">
        <div className="two-col">
          <section className="panel grow">
            <div className="panel-head">Datos de la Pregunta</div>
            <div className="panel-body tight">
              <div className="form-row">
                <CampoSelect
                  etiqueta="Competencia"
                  obligatorio
                  valor={competencia}
                  onChange={(v) => {
                    setCompetencia(v);
                    setTocado(true);
                  }}
                  opciones={competencias.map((c) => ({ valor: c.codigo, texto: c.nombre }))}
                />
                <CampoSelect
                  etiqueta="Factor"
                  obligatorio
                  valor={factor}
                  onChange={(v) => {
                    setFactor(v);
                    setTocado(true);
                  }}
                  deshabilitado={!competencia}
                  marcadorDeshabilitado="— Primero seleccione competencia —"
                  opciones={factores.map((f) => ({ valor: f.codigo, texto: f.nombre }))}
                />
              </div>
              <div className="form-row">
                <CampoTexto
                  etiqueta="Nombre de Pregunta"
                  obligatorio
                  ancho="full"
                  maxLargo={LARGO_NOMBRE}
                  valor={nombre}
                  onChange={(v) => {
                    setNombre(v);
                    setTocado(true);
                  }}
                />
              </div>
              <div className="form-row">
                <CampoArea
                  etiqueta="Pregunta"
                  obligatorio
                  ancho="full"
                  filas={3}
                  maxLargo={LARGO_ENUNCIADO}
                  valor={enunciado}
                  onChange={(v) => {
                    setEnunciado(v);
                    setTocado(true);
                  }}
                />
              </div>
              <div className="form-row">
                <CampoArea
                  etiqueta="Descripción"
                  opcional
                  ancho="full"
                  filas={2}
                  maxLargo={LARGO_DESCRIPCION}
                  valor={descripcion}
                  onChange={(v) => {
                    setDescripcion(v);
                    setTocado(true);
                  }}
                />
              </div>
              <ListaErrores
                errores={errores}
                titulo="No se pudo dar de alta la pregunta:"
                onCerrar={() => setErrores([])}
              />
            </div>
          </section>

          <section className="panel grow">
            <div className="panel-head">Opciones de Respuesta</div>
            <div className="panel-body tight">
              <div className="form-row">
                <CampoSelect
                  etiqueta="Plantilla de opciones"
                  opcional
                  ancho="full"
                  valor={plantilla}
                  onChange={elegirPlantilla}
                  marcador="— Cargar las opciones a mano —"
                  opciones={plantillas.map((p) => ({
                    valor: String(p.idPlantilla),
                    texto: p.nombre,
                  }))}
                />
              </div>

              {opciones.length === 0 ? (
                <div className="opt-empty">
                  Agregue al menos 2 opciones de respuesta, o elija una plantilla
                </div>
              ) : (
                <div className="tbl-wrap">
                  <table>
                    <thead>
                      <tr>
                        <th style={{ width: '90px' }}>Orden</th>
                        <th>Descripción</th>
                        <th style={{ width: '130px' }}>
                          Ponderación <span className="req-mark">*</span>
                        </th>
                        <th style={{ width: '60px' }} />
                      </tr>
                    </thead>
                    <tbody>
                      {opciones.map((o, i) => (
                        <tr key={i}>
                          <td>
                            <input
                              className={`opt-input${o.orden ? ' filled' : ''}`}
                              value={o.orden}
                              onChange={(e) => cambiarOpcion(i, 'orden', e.target.value)}
                            />
                          </td>
                          <td>
                            <input
                              className={`opt-input${o.descripcion ? ' filled' : ''}`}
                              maxLength={LARGO_DESCRIPCION_OPCION}
                              value={o.descripcion}
                              onChange={(e) => cambiarOpcion(i, 'descripcion', e.target.value)}
                            />
                          </td>
                          <td>
                            <input
                              className={`opt-input${o.ponderacion ? ' filled' : ''}`}
                              placeholder="0"
                              value={o.ponderacion}
                              onChange={(e) => cambiarOpcion(i, 'ponderacion', e.target.value)}
                            />
                          </td>
                          <td style={{ textAlign: 'center' }}>
                            <Boton
                              tipo="danger"
                              chico
                              onClick={() => quitarOpcion(i)}
                              titulo="Quitar esta opción"
                            >
                              ✕
                            </Boton>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}

              <div className="opt-foot">
                <Boton
                  chico
                  tipo="primary"
                  onClick={agregarOpcion}
                  deshabilitado={opciones.length >= MAXIMO_OPCIONES}
                >
                  + Agregar opción
                </Boton>
                <span>
                  Suma actual:{' '}
                  <span className={`sum ${sumaOk ? 'ok' : 'bad'}`}>
                    {Math.round(suma * 100) / 100} / 10 {sumaOk ? '✓' : '✗'}
                  </span>
                </span>
              </div>

              <Mensaje tipo="info" estilo={{ marginTop: 12 }}>
                La plantilla solo precarga la grilla: las opciones se copian a la pregunta.
              </Mensaje>
            </div>
            <div className="action-bar">
              <Boton tipo="warn" deshabilitado titulo="CU033: no entra en esta entrega">
                ✦ Generar con IA
              </Boton>
              <Boton
                tipo="primary"
                onClick={aceptar}
                deshabilitado={guardando}
                estilo={{ marginLeft: 'auto' }}
              >
                ✔ Aceptar
              </Boton>
              <Boton onClick={cancelar}>✕ Cancelar</Boton>
            </div>
          </section>
        </div>
      </div>

      {modal === 'guardada' && (
        <Modal
          titulo="¡Pregunta creada correctamente!"
          colorTitulo="var(--success)"
          centrado
          acciones={
            <>
              <Boton tipo="success" onClick={limpiar}>
                SÍ
              </Boton>
              <Boton onClick={() => router.push('/preguntas')}>NO</Boton>
            </>
          }
        >
          La pregunta <strong>«{guardada}»</strong> se ha creado correctamente.
          <br />
          ¿Desea cargar otra pregunta?
        </Modal>
      )}

      {modal === 'cancelar' && (
        <Modal
          titulo="⚠ Confirmar Cancelación"
          colorTitulo="var(--warn)"
          acciones={
            <>
              <Boton onClick={() => setModal(null)}>No, Continuar</Boton>
              <Boton tipo="danger" onClick={() => router.push('/preguntas')}>
                Sí, Cancelar
              </Boton>
            </>
          }
        >
          ¿Está seguro de que desea cancelar? Los cambios no guardados se perderán.
        </Modal>
      )}
    </div>
  );
}
