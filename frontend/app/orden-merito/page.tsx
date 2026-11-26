'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';

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

type PuestoDTO = { codigo: string; nombre: string; empresa: string };
type EvaluacionDTO = { idEvaluacion: number; fechaCreacion: string };
type OrdenMeritoDTO = {
  tipoDocumento: string;
  nroDocumento: string;
  apellido: string;
  nombre: string;
  puntajeObtenido: number | null;
  estadoCuestionario: string;
  fechaInicio: string | null;
  fechaFin: string | null;
  ultimoIngreso: string | null;
  cantidadAccesos: number;
  apto: boolean;
};

function fecha(valor: string | null): string {
  if (!valor) {
    return '—';
  }
  const d = new Date(valor);
  const dos = (n: number) => String(n).padStart(2, '0');
  return `${dos(d.getDate())}/${dos(d.getMonth() + 1)}/${d.getFullYear()} ${dos(d.getHours())}:${dos(d.getMinutes())}`;
}

function etiquetaEstado(estado: string): string {
  return estado.charAt(0) + estado.slice(1).toLowerCase().replace('_', ' ');
}

/** CU028: emitir el orden de mérito de un puesto. */
export default function PantallaOrdenMerito() {
  const router = useRouter();
  const usuario = useConsultor();

  const [paso, setPaso] = useState(1);
  const [errores, setErrores] = useState<string[]>([]);
  const [modalSinEvaluaciones, setModalSinEvaluaciones] = useState(false);

  const [codigo, setCodigo] = useState('');
  const [nombre, setNombre] = useState('');
  const [empresa, setEmpresa] = useState('');
  const [puestos, setPuestos] = useState<PuestoDTO[]>([]);
  const [buscado, setBuscado] = useState(false);
  const [puesto, setPuesto] = useState<PuestoDTO | null>(null);

  const [evaluaciones, setEvaluaciones] = useState<EvaluacionDTO[]>([]);
  const [elegidas, setElegidas] = useState<number[]>([]);

  const [ordenMerito, setOrdenMerito] = useState<OrdenMeritoDTO[]>([]);
  const [generadoEl, setGeneradoEl] = useState('');

  async function buscar() {
    setErrores([]);
    try {
      const parametros = new URLSearchParams();
      if (codigo.trim()) parametros.set('codigo', codigo.trim());
      if (nombre.trim()) parametros.set('nombre', nombre.trim());
      if (empresa.trim()) parametros.set('empresa', empresa.trim());
      setPuestos(await api.get<PuestoDTO[]>(`/reportes/orden-merito/puestos?${parametros}`));
      setPuesto(null);
      setBuscado(true);
    } catch (e) {
      setErrores(mensajesDeError(e));
    }
  }

  async function emitir() {
    if (!puesto) {
      return;
    }
    setErrores([]);
    try {
      const datos = await api.get<EvaluacionDTO[]>(
        `/reportes/orden-merito/puestos/${puesto.codigo}/evaluaciones`,
      );
      if (datos.length === 0) {
        setModalSinEvaluaciones(true);
        return;
      }
      setEvaluaciones(datos);
      setElegidas(datos.map((e) => e.idEvaluacion));
      setPaso(2);
    } catch (e) {
      setErrores(mensajesDeError(e));
    }
  }

  async function generar() {
    setErrores([]);
    try {
      const datos = await api.post<OrdenMeritoDTO[]>('/reportes/orden-merito', {
        codigoPuesto: puesto!.codigo,
        idsEvaluaciones: elegidas,
      });
      setOrdenMerito(datos);
      setGeneradoEl(fecha(new Date().toISOString()));
      setPaso(3);
    } catch (e) {
      setErrores(mensajesDeError(e));
    }
  }

  async function imprimir() {
    setErrores([]);
    try {
      await api.descargar('/reportes/orden-merito/impresion', {
        codigoPuesto: puesto!.codigo,
        idsEvaluaciones: elegidas,
        nombrePuesto: puesto!.nombre,
      });
    } catch (e) {
      setErrores(mensajesDeError(e));
    }
  }

  if (!usuario) {
    return <div className="cargando">Cargando…</div>;
  }

  const aptos = ordenMerito.filter((f) => f.apto);
  const noAptos = ordenMerito.filter((f) => !f.apto);
  const todas = elegidas.length === evaluaciones.length && evaluaciones.length > 0;

  return (
    <div className="pantalla">
      <Encabezado
        migas={[
          { texto: 'Inicio', ruta: '/inicio' },
          { texto: 'Reportes' },
          { texto: 'Emitir Orden de Mérito' },
        ]}
        usuario={usuario}
        inicialesUsuario={iniciales(usuario)}
      />
      <div className="content">
        <ListaErrores errores={errores} onCerrar={() => setErrores([])} />

        {paso === 1 && (
          <>
            <Panel titulo="Búsqueda de Puesto / Función" ajustado>
              <div className="form-row">
                <CampoTexto etiqueta="Código" ancho="narrow" valor={codigo} onChange={setCodigo} onEnter={buscar} />
                <CampoTexto etiqueta="Nombre del Puesto" valor={nombre} onChange={setNombre} onEnter={buscar} />
                <CampoTexto etiqueta="Empresa" valor={empresa} onChange={setEmpresa} onEnter={buscar} />
              </div>
              <div className="btn-row">
                <Boton tipo="primary" onClick={buscar}>
                  🔍 Buscar
                </Boton>
                <Boton onClick={() => router.push('/inicio')} estilo={{ marginLeft: 'auto' }}>
                  ✕ Cancelar
                </Boton>
              </div>
            </Panel>

            <section className="panel grow">
              <div className="panel-head">
                Resultados
                {buscado && (
                  <span style={{ fontWeight: 'normal', textTransform: 'none', color: '#555' }}>
                    {' '}
                    — {puestos.length} puesto(s) encontrado(s)
                  </span>
                )}
              </div>
              <Grilla
                columnas={[
                  { titulo: 'Código', ancho: '130px' },
                  { titulo: 'Nombre del Puesto' },
                  { titulo: 'Empresa', ancho: '240px' },
                ]}
                filas={puestos.length}
                buscado={buscado}
                textoVacio="Ejecute una búsqueda para ver resultados"
                textoSinResultados="No se encontraron puestos"
              >
                {puestos.map((p) => (
                  <tr
                    key={p.codigo}
                    className={`clk${puesto?.codigo === p.codigo ? ' selected' : ''}`}
                    onClick={() => setPuesto(puesto?.codigo === p.codigo ? null : p)}
                  >
                    <td>{p.codigo}</td>
                    <td>{p.nombre}</td>
                    <td>{p.empresa}</td>
                  </tr>
                ))}
              </Grilla>
              <div className="action-bar">
                <Boton
                  tipo="primary"
                  onClick={emitir}
                  deshabilitado={!puesto}
                  estilo={{ marginLeft: 'auto' }}
                >
                  Emitir Orden de Mérito →
                </Boton>
              </div>
            </section>
          </>
        )}

        {paso === 2 && puesto && (
          <section className="panel" style={{ maxWidth: 900, width: '100%', margin: '0 auto' }}>
            <div className="panel-head">Seleccionar Evaluaciones — {puesto.nombre}</div>
            <div className="panel-body">
              <div style={{ color: 'var(--mid)', marginBottom: 14 }}>
                Seleccione las evaluaciones a incluir en el orden de mérito:
              </div>
              <table>
                <thead>
                  <tr>
                    <th style={{ width: 70 }} />
                    <th>Identificador</th>
                    <th>Fecha</th>
                  </tr>
                </thead>
                <tbody>
                  {evaluaciones.map((e) => {
                    const marcada = elegidas.includes(e.idEvaluacion);
                    return (
                      <tr
                        key={e.idEvaluacion}
                        className={`clk${marcada ? ' selected' : ''}`}
                        onClick={() =>
                          setElegidas(
                            marcada
                              ? elegidas.filter((x) => x !== e.idEvaluacion)
                              : [...elegidas, e.idEvaluacion],
                          )
                        }
                      >
                        <td style={{ textAlign: 'center' }}>
                          <span className={`chk${marcada ? ' on' : ''}`}>{marcada ? '✔' : ''}</span>
                        </td>
                        <td>Evaluación {e.idEvaluacion}</td>
                        <td>{fecha(e.fechaCreacion)}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
              <div
                style={{ marginTop: 14, display: 'flex', alignItems: 'center', gap: 10, cursor: 'pointer' }}
                onClick={() => setElegidas(todas ? [] : evaluaciones.map((e) => e.idEvaluacion))}
              >
                <span className={`chk${todas ? ' on' : ''}`} style={{ margin: 0 }}>
                  {todas ? '✔' : ''}
                </span>
                <span>TODAS las evaluaciones</span>
              </div>
              {elegidas.length === 0 && (
                <Mensaje tipo="error">Debe seleccionar al menos una evaluación.</Mensaje>
              )}
            </div>
            <div className="action-bar">
              <Boton onClick={() => setPaso(1)}>← Atrás</Boton>
              <Boton
                tipo="primary"
                onClick={generar}
                deshabilitado={elegidas.length === 0}
                estilo={{ marginLeft: 'auto' }}
              >
                Generar →
              </Boton>
              <Boton onClick={() => router.push('/inicio')}>✕ Cancelar</Boton>
            </div>
          </section>
        )}

        {paso === 3 && puesto && (
          <>
            <Panel titulo={`Orden de Mérito — ${puesto.nombre}`} ajustado>
              <div style={{ display: 'flex', gap: 36, flexWrap: 'wrap' }}>
                <div>
                  <span style={{ color: 'var(--mid)' }}>Empresa:</span> <strong>{puesto.empresa}</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--mid)' }}>Fecha de generación:</span>{' '}
                  <strong>{generadoEl}</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--mid)' }}>Evaluaciones incluidas:</span>{' '}
                  <strong>{todas ? 'TODAS' : elegidas.join(', ')}</strong>
                </div>
              </div>
            </Panel>

            <section className="panel">
              <div className="panel-head" style={{ color: 'var(--success)' }}>
                ✔ Candidatos Aptos
              </div>
              <Grilla
                columnas={[
                  { titulo: 'Pos.', ancho: '70px', centrada: true },
                  { titulo: 'Tipo Doc.', ancho: '100px' },
                  { titulo: 'Nro Documento', ancho: '150px' },
                  { titulo: 'Apellido' },
                  { titulo: 'Nombre' },
                  { titulo: 'Puntaje', ancho: '100px', centrada: true },
                  { titulo: 'Fecha Inicio' },
                  { titulo: 'Fecha Fin' },
                  { titulo: 'Accesos', ancho: '90px', centrada: true },
                ]}
                filas={aptos.length}
                textoVacio="Ningún candidato apto"
              >
                {aptos.map((f, i) => (
                  <tr key={`${f.tipoDocumento}-${f.nroDocumento}`} className="apto-row">
                    <td className="apto-pos">{i + 1}</td>
                    <td>{f.tipoDocumento}</td>
                    <td>{f.nroDocumento}</td>
                    <td>{f.apellido}</td>
                    <td>{f.nombre}</td>
                    <td style={{ textAlign: 'center', fontWeight: 'bold' }}>
                      {f.puntajeObtenido?.toFixed(2)}
                    </td>
                    <td>{fecha(f.fechaInicio)}</td>
                    <td>{fecha(f.fechaFin)}</td>
                    <td style={{ textAlign: 'center' }}>{f.cantidadAccesos}</td>
                  </tr>
                ))}
              </Grilla>
            </section>

            <section className="panel">
              <div className="panel-head" style={{ color: 'var(--danger)' }}>
                ✕ Candidatos No Aptos
              </div>
              <Grilla
                columnas={[
                  { titulo: 'Tipo Doc.', ancho: '100px' },
                  { titulo: 'Nro Documento', ancho: '150px' },
                  { titulo: 'Apellido' },
                  { titulo: 'Nombre' },
                  { titulo: 'Puntaje', ancho: '100px', centrada: true },
                  { titulo: 'Estado', ancho: '150px' },
                  { titulo: 'Fecha Inicio' },
                  { titulo: 'Último ingreso' },
                  { titulo: 'Accesos', ancho: '90px', centrada: true },
                ]}
                filas={noAptos.length}
                textoVacio="Sin candidatos no aptos"
              >
                {noAptos.map((f) => (
                  <tr key={`${f.tipoDocumento}-${f.nroDocumento}`} className="no-apto-row">
                    <td>{f.tipoDocumento}</td>
                    <td>{f.nroDocumento}</td>
                    <td>{f.apellido}</td>
                    <td>{f.nombre}</td>
                    <td style={{ textAlign: 'center' }}>
                      {f.puntajeObtenido === null ? '—' : f.puntajeObtenido.toFixed(2)}
                    </td>
                    <td>
                      <span className={`estado-badge badge-${f.estadoCuestionario.toLowerCase()}`}>
                        {etiquetaEstado(f.estadoCuestionario)}
                      </span>
                    </td>
                    <td>{fecha(f.fechaInicio)}</td>
                    <td>{fecha(f.ultimoIngreso)}</td>
                    <td style={{ textAlign: 'center' }}>{f.cantidadAccesos}</td>
                  </tr>
                ))}
              </Grilla>
            </section>

            <div className="btn-row">
              <Boton onClick={() => setPaso(2)}>← Atrás</Boton>
              <span style={{ color: 'var(--mid)', fontStyle: 'italic' }}>
                Apto = cuestionario completo que alcanza la ponderación mínima de cada competencia
                del puesto
              </span>
              <Boton tipo="primary" onClick={imprimir} estilo={{ marginLeft: 'auto' }}>
                🖨 Imprimir
              </Boton>
              <Boton onClick={() => router.push('/inicio')}>✕ Cerrar</Boton>
            </div>
          </>
        )}
      </div>

      {modalSinEvaluaciones && (
        <Modal
          titulo="⚠ Sin evaluaciones"
          colorTitulo="var(--warn)"
          acciones={
            <Boton tipo="primary" onClick={() => setModalSinEvaluaciones(false)}>
              Aceptar
            </Boton>
          }
        >
          El puesto <strong>{puesto?.nombre}</strong> no posee evaluaciones registradas.
        </Modal>
      )}
    </div>
  );
}
