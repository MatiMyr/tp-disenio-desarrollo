'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';

import AsistentePasos from '@/components/AsistentePasos';
import Boton from '@/components/Boton';
import { CampoTexto } from '@/components/Campo';
import Encabezado from '@/components/Encabezado';
import Grilla from '@/components/Grilla';
import Mensaje, { ListaErrores } from '@/components/Mensaje';
import Modal from '@/components/Modal';
import Panel from '@/components/Panel';
import { useConsultor } from '@/components/SesionConsultor';
import { api, mensajesDeError } from '@/lib/api';
import { iniciales } from '@/lib/sesion';

type CandidatoDTO = {
  nroCandidato: number;
  apellido: string;
  nombre: string;
  tipoDocumento: string;
  nroDocumento: string;
};
type CandidatoClaveDTO = CandidatoDTO & { clave: string };
type PuestoDTO = { codigo: string; nombre: string; empresa: string };
type CompetenciaEvaluableDTO = {
  nombreCompetencia: string;
  ponderacionMinima: number;
  evaluable: boolean;
};

const PASOS = ['Buscar Candidatos', 'Candidatos a Evaluar', 'Seleccionar Puesto', 'Confirmar'];

/** CU025: asistente de 4 pasos para evaluar candidatos. */
export default function PantallaEvaluarCandidatos() {
  const router = useRouter();
  const usuario = useConsultor();

  const [paso, setPaso] = useState(1);
  const [errores, setErrores] = useState<string[]>([]);
  const [aviso, setAviso] = useState<string | null>(null);
  const [modal, setModal] = useState<'noEvaluables' | 'generada' | 'cancelar' | null>(null);

  // Paso 1
  const [apellido, setApellido] = useState('');
  const [nombre, setNombre] = useState('');
  const [nro, setNro] = useState('');
  const [encontrados, setEncontrados] = useState<CandidatoDTO[]>([]);
  const [buscado, setBuscado] = useState(false);
  const [marcados, setMarcados] = useState<number[]>([]);

  // Paso 2
  const [aEvaluar, setAEvaluar] = useState<CandidatoDTO[]>([]);
  const [marcadosLista, setMarcadosLista] = useState<number[]>([]);
  const [conCuestionario, setConCuestionario] = useState<number[]>([]);

  // Paso 3
  const [puestos, setPuestos] = useState<PuestoDTO[]>([]);
  const [puesto, setPuesto] = useState<PuestoDTO | null>(null);
  const [competencias, setCompetencias] = useState<CompetenciaEvaluableDTO[]>([]);

  // Paso 4
  const [claves, setClaves] = useState<CandidatoClaveDTO[]>([]);

  useEffect(() => {
    if (paso === 3 && puestos.length === 0) {
      api.get<PuestoDTO[]>('/puestos').then(setPuestos).catch((e) => setErrores(mensajesDeError(e)));
    }
  }, [paso, puestos.length]);

  async function buscarCandidatos() {
    setErrores([]);
    try {
      const parametros = new URLSearchParams();
      if (apellido.trim()) parametros.set('apellido', apellido.trim());
      if (nombre.trim()) parametros.set('nombre', nombre.trim());
      if (nro.trim()) parametros.set('nroCandidato', nro.trim());
      setEncontrados(await api.get<CandidatoDTO[]>(`/candidatos?${parametros.toString()}`));
      setMarcados([]);
      setBuscado(true);
    } catch (e) {
      setErrores(mensajesDeError(e));
    }
  }

  function agregarSeleccionados() {
    if (marcados.length === 0) {
      setAviso('Debe seleccionar uno o más candidatos');
      return;
    }
    const nuevos = encontrados.filter(
      (c) => marcados.includes(c.nroCandidato) && !aEvaluar.some((x) => x.nroCandidato === c.nroCandidato),
    );
    setAEvaluar([...aEvaluar, ...nuevos]);
    setMarcados([]);
    setAviso(null);
  }

  /** Paso 6.A: antes de seguir se controla que ninguno tenga cuestionario activo. */
  async function verificarYSeguir() {
    setErrores([]);
    try {
      const ocupados = await api.post<number[]>('/evaluaciones/verificar-candidatos', {
        nrosCandidato: aEvaluar.map((c) => c.nroCandidato),
      });
      setConCuestionario(ocupados);
      if (ocupados.length > 0) {
        return;
      }
      setPaso(3);
    } catch (e) {
      setErrores(mensajesDeError(e));
    }
  }

  async function elegirPuesto(p: PuestoDTO) {
    setPuesto(p);
    setErrores([]);
    try {
      const datos = await api.get<CompetenciaEvaluableDTO[]>(
        `/evaluaciones/puestos/${p.codigo}/competencias`,
      );
      setCompetencias(datos);
      if (datos.some((c) => !c.evaluable)) {
        setModal('noEvaluables');
      }
    } catch (e) {
      setErrores(mensajesDeError(e));
    }
  }

  async function generarClaves() {
    setErrores([]);
    try {
      setClaves(
        await api.post<CandidatoClaveDTO[]>('/evaluaciones/claves', { candidatos: aEvaluar }),
      );
      setPaso(4);
    } catch (e) {
      setErrores(mensajesDeError(e));
    }
  }

  async function finalizar() {
    setErrores([]);
    try {
      await api.post('/evaluaciones', { codigoPuesto: puesto!.codigo, claves });
      setModal('generada');
    } catch (e) {
      setErrores(mensajesDeError(e));
    }
  }

  async function exportarExcel() {
    try {
      await api.descargar('/evaluaciones/claves/excel', {
        nombrePuesto: puesto!.nombre,
        claves,
      });
      router.push('/inicio');
    } catch (e) {
      setErrores(mensajesDeError(e));
    }
  }

  function alternar(lista: number[], valor: number, set: (v: number[]) => void) {
    set(lista.includes(valor) ? lista.filter((x) => x !== valor) : [...lista, valor]);
  }

  if (!usuario) {
    return <div className="cargando">Cargando…</div>;
  }

  const hayNoEvaluables = competencias.some((c) => !c.evaluable);

  return (
    <div className="pantalla">
      <Encabezado
        migas={[
          { texto: 'Inicio', ruta: '/inicio' },
          { texto: 'Evaluaciones' },
          { texto: 'Evaluar a Candidatos' },
        ]}
        usuario={usuario}
        inicialesUsuario={iniciales(usuario)}
      />
      <AsistentePasos pasos={PASOS} actual={paso} />

      <div className="content">
        <ListaErrores errores={errores} onCerrar={() => setErrores([])} />

        {paso === 1 && (
          <>
            <Panel titulo="Búsqueda de Candidatos" ajustado>
              <div className="form-row">
                <CampoTexto etiqueta="Apellido" valor={apellido} onChange={setApellido} onEnter={buscarCandidatos} />
                <CampoTexto etiqueta="Nombre" valor={nombre} onChange={setNombre} onEnter={buscarCandidatos} />
                <CampoTexto
                  etiqueta="Número de Candidato"
                  valor={nro}
                  onChange={setNro}
                  onEnter={buscarCandidatos}
                />
              </div>
              <div className="btn-row">
                <Boton tipo="primary" onClick={buscarCandidatos}>
                  🔍 Buscar
                </Boton>
                <Boton onClick={() => setModal('cancelar')} estilo={{ marginLeft: 'auto' }}>
                  ✕ Cancelar
                </Boton>
              </div>
              <div className="form-hint" style={{ margin: '10px 0 0' }}>
                ✎ Dejá los campos vacíos para listar todos los candidatos
              </div>
              {aviso && (
                <Mensaje tipo="error" onCerrar={() => setAviso(null)}>
                  {aviso}
                </Mensaje>
              )}
            </Panel>

            <section className="panel grow">
              <div className="panel-head">
                Candidatos encontrados
                {buscado && (
                  <span style={{ fontWeight: 'normal', textTransform: 'none', color: '#555' }}>
                    {' '}
                    — {encontrados.length} resultado(s)
                  </span>
                )}
              </div>
              <Grilla
                columnas={[
                  { titulo: '', ancho: '70px', centrada: true },
                  { titulo: 'Nro. Candidato' },
                  { titulo: 'Apellido' },
                  { titulo: 'Nombre' },
                  { titulo: 'Documento' },
                ]}
                filas={encontrados.length}
                buscado={buscado}
                textoVacio="Ingrese criterios de búsqueda y presione Buscar para ver resultados"
                textoSinResultados="No se encontraron candidatos con esos criterios"
              >
                {encontrados.map((c) => {
                  const enLista = aEvaluar.some((x) => x.nroCandidato === c.nroCandidato);
                  const marcado = marcados.includes(c.nroCandidato);
                  return (
                    <tr
                      key={c.nroCandidato}
                      className={`${enLista ? 'muted' : 'clk'}${marcado ? ' selected' : ''}`}
                      onClick={() =>
                        !enLista && alternar(marcados, c.nroCandidato, setMarcados)
                      }
                    >
                      <td style={{ textAlign: 'center' }}>
                        {enLista ? (
                          <span style={{ fontSize: 12, color: 'var(--success)' }}>en lista</span>
                        ) : (
                          <span className={`chk${marcado ? ' on' : ''}`}>{marcado ? '✔' : ''}</span>
                        )}
                      </td>
                      <td>{c.nroCandidato}</td>
                      <td>{c.apellido}</td>
                      <td>{c.nombre}</td>
                      <td>
                        {c.tipoDocumento} {c.nroDocumento}
                      </td>
                    </tr>
                  );
                })}
              </Grilla>
              <div className="action-bar">
                <Boton tipo="primary" onClick={agregarSeleccionados} deshabilitado={!buscado}>
                  + Agregar Seleccionados
                </Boton>
                <span style={{ color: 'var(--primary)', fontStyle: 'italic' }}>
                  {aEvaluar.length > 0 && `${aEvaluar.length} en la lista a evaluar`}
                </span>
                <Boton
                  tipo="primary"
                  onClick={() => setPaso(2)}
                  deshabilitado={aEvaluar.length === 0}
                  estilo={{ marginLeft: 'auto' }}
                >
                  Siguiente →
                </Boton>
              </div>
            </section>
          </>
        )}

        {paso === 2 && conCuestionario.length === 0 && (
          <section className="panel grow">
            <div className="panel-head">
              Candidatos a evaluar
              <span style={{ fontWeight: 'normal', textTransform: 'none', color: '#555' }}>
                {' '}
                — {aEvaluar.length} candidato(s)
              </span>
            </div>
            <Grilla
              columnas={[
                { titulo: '', ancho: '70px', centrada: true },
                { titulo: 'Nro. Candidato' },
                { titulo: 'Apellido' },
                { titulo: 'Nombre' },
                { titulo: 'Documento' },
              ]}
              filas={aEvaluar.length}
              textoVacio="La lista está vacía. Use “+ Agregar más” para buscar candidatos."
            >
              {aEvaluar.map((c) => {
                const marcado = marcadosLista.includes(c.nroCandidato);
                return (
                  <tr
                    key={c.nroCandidato}
                    className={`clk${marcado ? ' selected' : ''}`}
                    onClick={() => alternar(marcadosLista, c.nroCandidato, setMarcadosLista)}
                  >
                    <td style={{ textAlign: 'center' }}>
                      <span className={`chk${marcado ? ' on' : ''}`}>{marcado ? '✔' : ''}</span>
                    </td>
                    <td>{c.nroCandidato}</td>
                    <td>{c.apellido}</td>
                    <td>{c.nombre}</td>
                    <td>
                      {c.tipoDocumento} {c.nroDocumento}
                    </td>
                  </tr>
                );
              })}
            </Grilla>
            <div className="action-bar">
              <span style={{ color: 'var(--primary)', fontStyle: 'italic', flex: 1 }}>
                {marcadosLista.length} candidato(s) seleccionado(s)
              </span>
              {marcadosLista.length > 0 ? (
                <Boton
                  tipo="danger"
                  chico
                  onClick={() => {
                    setAEvaluar(aEvaluar.filter((c) => !marcadosLista.includes(c.nroCandidato)));
                    setMarcadosLista([]);
                  }}
                >
                  ✖ Quitar Seleccionados
                </Boton>
              ) : (
                <Boton tipo="primary" chico onClick={() => setPaso(1)}>
                  + Agregar más
                </Boton>
              )}
            </div>
            <div className="action-bar">
              <Boton onClick={() => setPaso(1)}>← Atrás</Boton>
              <Boton
                tipo="primary"
                onClick={verificarYSeguir}
                deshabilitado={aEvaluar.length === 0}
                estilo={{ marginLeft: 'auto' }}
              >
                Siguiente →
              </Boton>
              <Boton onClick={() => setModal('cancelar')}>✕ Cancelar</Boton>
            </div>
          </section>
        )}

        {paso === 2 && conCuestionario.length > 0 && (
          <section className="panel grow">
            <div className="panel-head" style={{ color: 'var(--danger)' }}>
              ✖ No se puede continuar: cuestionarios activos o en proceso
            </div>
            <div className="panel-body tight">
              <Mensaje tipo="error" estilo={{ marginTop: 0 }}>
                Los siguientes candidatos ya poseen cuestionarios activos o en proceso y no pueden
                ser evaluados nuevamente hasta que dichos cuestionarios sean finalizados o
                cancelados:
              </Mensaje>
              <table>
                <thead>
                  <tr>
                    <th>Nro. Candidato</th>
                    <th>Apellido</th>
                    <th>Nombre</th>
                    <th>Documento</th>
                  </tr>
                </thead>
                <tbody>
                  {aEvaluar
                    .filter((c) => conCuestionario.includes(c.nroCandidato))
                    .map((c) => (
                      <tr key={c.nroCandidato}>
                        <td>{c.nroCandidato}</td>
                        <td>{c.apellido}</td>
                        <td>{c.nombre}</td>
                        <td>
                          {c.tipoDocumento} {c.nroDocumento}
                        </td>
                      </tr>
                    ))}
                </tbody>
              </table>
              <div className="form-hint" style={{ marginTop: 14 }}>
                ✎ Al volver, quitá esos candidatos de la lista para poder continuar.
              </div>
            </div>
            <div className="action-bar">
              <Boton
                tipo="primary"
                onClick={() => {
                  setMarcadosLista(conCuestionario);
                  setConCuestionario([]);
                }}
              >
                ← Volver
              </Boton>
            </div>
          </section>
        )}

        {paso === 3 && (
          <div className="content row" style={{ padding: 0, gap: 16 }}>
            <div style={{ flex: 1.4, minWidth: 320, display: 'flex' }}>
              <section className="panel grow" style={{ width: '100%' }}>
                <div className="panel-head">Seleccionar Función / Puesto</div>
                <Grilla
                  columnas={[{ titulo: 'Código', ancho: '110px' }, { titulo: 'Nombre del Puesto' }, { titulo: 'Empresa' }]}
                  filas={puestos.length}
                  textoVacio="No hay puestos cargados"
                >
                  {puestos.map((p) => (
                    <tr
                      key={p.codigo}
                      className={`clk${puesto?.codigo === p.codigo ? ' selected' : ''}`}
                      onClick={() => elegirPuesto(p)}
                    >
                      <td>{p.codigo}</td>
                      <td>{p.nombre}</td>
                      <td>{p.empresa}</td>
                    </tr>
                  ))}
                </Grilla>
                <div className="action-bar">
                  <Boton onClick={() => setPaso(2)}>← Atrás</Boton>
                  <Boton
                    tipo="primary"
                    onClick={generarClaves}
                    deshabilitado={!puesto || hayNoEvaluables}
                    estilo={{ marginLeft: 'auto' }}
                  >
                    Siguiente →
                  </Boton>
                  <Boton onClick={() => setModal('cancelar')}>✕ Cancelar</Boton>
                </div>
              </section>
            </div>
            <div style={{ flex: 1, minWidth: 280 }}>
              <Panel titulo="Competencias requeridas" ajustado>
                {puesto ? (
                  <>
                    <div style={{ color: 'var(--mid)', fontStyle: 'italic', marginBottom: 10 }}>
                      Puesto: {puesto.nombre} — {puesto.empresa}
                    </div>
                    <table className="comp-table">
                      <thead>
                        <tr>
                          <th>Competencia</th>
                          <th style={{ width: 110, textAlign: 'center' }}>Ponderación</th>
                          <th style={{ width: 110, textAlign: 'center' }}>Evaluable</th>
                        </tr>
                      </thead>
                      <tbody>
                        {competencias.map((c) => (
                          <tr key={c.nombreCompetencia}>
                            <td>{c.nombreCompetencia}</td>
                            <td style={{ textAlign: 'center' }}>{c.ponderacionMinima}</td>
                            <td style={{ textAlign: 'center' }}>
                              {c.evaluable ? '✔' : <strong style={{ color: 'var(--danger)' }}>✖</strong>}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </>
                ) : (
                  <div className="opt-empty">Seleccioná un puesto para ver sus competencias</div>
                )}
              </Panel>
            </div>
          </div>
        )}

        {paso === 4 && puesto && (
          <>
            <Panel titulo="Función seleccionada" ajustado>
              <div className="kv">
                <div>
                  <span className="k">Puesto:</span>
                  <strong>{puesto.nombre}</strong>
                </div>
                <div>
                  <span className="k">Empresa:</span>
                  <strong>{puesto.empresa}</strong>
                </div>
              </div>
            </Panel>
            <section className="panel grow">
              <div className="panel-head">Candidatos a evaluar</div>
              <Grilla
                columnas={[
                  { titulo: 'Apellido' },
                  { titulo: 'Nombre' },
                  { titulo: 'Tipo Doc.' },
                  { titulo: 'Nro. Documento' },
                  { titulo: 'Clave de Ingreso' },
                ]}
                filas={claves.length}
                textoVacio="No hay candidatos"
              >
                {claves.map((c) => (
                  <tr key={c.nroCandidato}>
                    <td>{c.apellido}</td>
                    <td>{c.nombre}</td>
                    <td>{c.tipoDocumento}</td>
                    <td>{c.nroDocumento}</td>
                    <td
                      style={{
                        fontFamily: 'monospace',
                        letterSpacing: '0.06em',
                        color: 'var(--primary-dk)',
                      }}
                    >
                      {c.clave}
                    </td>
                  </tr>
                ))}
              </Grilla>
              <div className="action-bar">
                <Boton onClick={() => setPaso(3)}>← Atrás</Boton>
                <Boton tipo="success" onClick={finalizar} estilo={{ marginLeft: 'auto' }}>
                  ✔ Finalizar
                </Boton>
                <Boton onClick={() => setModal('cancelar')}>✕ Cancelar</Boton>
              </div>
            </section>
          </>
        )}
      </div>

      {modal === 'noEvaluables' && (
        <Modal
          titulo="⚠ Competencias que no pueden evaluarse"
          colorTitulo="var(--warn)"
          acciones={
            <Boton tipo="primary" onClick={() => router.push('/inicio')}>
              ✔ Aceptar
            </Boton>
          }
        >
          Las siguientes competencias del puesto <strong>{puesto?.nombre}</strong> no tienen ningún
          factor con 2 o más preguntas, así que no pueden ser evaluadas:
          <ul style={{ marginTop: 8, marginLeft: 20 }}>
            {competencias
              .filter((c) => !c.evaluable)
              .map((c) => (
                <li key={c.nombreCompetencia}>{c.nombreCompetencia}</li>
              ))}
          </ul>
        </Modal>
      )}

      {modal === 'generada' && (
        <Modal
          titulo="✔ Cuestionarios generados exitosamente"
          colorTitulo="var(--success)"
          acciones={
            <>
              <Boton onClick={() => router.push('/inicio')}>No exportar</Boton>
              <Boton tipo="success" onClick={exportarExcel}>
                📄 Exportar a Excel
              </Boton>
            </>
          }
        >
          Se han generado los cuestionarios para los <strong>{claves.length}</strong> candidatos
          seleccionados para el puesto <strong>{puesto?.nombre}</strong>.
          <br />
          <br />
          ¿Desea exportar la lista de candidatos con sus claves de ingreso a un archivo Excel?
        </Modal>
      )}

      {modal === 'cancelar' && (
        <Modal
          titulo="⚠ Confirmar Cancelación"
          colorTitulo="var(--warn)"
          acciones={
            <>
              <Boton onClick={() => setModal(null)}>No, Continuar</Boton>
              <Boton tipo="danger" onClick={() => router.push('/inicio')}>
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
