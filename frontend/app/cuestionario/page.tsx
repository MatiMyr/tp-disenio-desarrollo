'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';

import Boton from '@/components/Boton';
import Mensaje, { ListaErrores } from '@/components/Mensaje';
import Panel from '@/components/Panel';
import { api, mensajesDeError } from '@/lib/api';
import { cerrarSesion, cuestionarioActual } from '@/lib/sesion';

type CuestionarioDTO = {
  nombreCandidato: string;
  idCuestionario: number;
  estado: string;
  tiempoRestante: number;
  instrucciones: string;
};
type OpcionDTO = { idOpcion: number; orden: number; descripcion: string };
type PreguntaDTO = {
  idPreguntaCuestionario: number;
  enunciado: string;
  opciones: OpcionDTO[];
};
type WizardDTO = {
  idCuestionario: number;
  nroBloqueActual: number;
  cantidadBloquesTotales: number;
  preguntas: PreguntaDTO[];
};

type Fase = 'cargando' | 'instrucciones' | 'bloque' | 'vencido' | 'fin';

const ESTADOS_VENCIDOS = ['INCOMPLETO', 'SIN_CONTESTAR', 'COMPLETO'];

/** CU026: instrucciones, bloques de preguntas y cierre del cuestionario. */
export default function PantallaCuestionario() {
  const router = useRouter();

  const [fase, setFase] = useState<Fase>('cargando');
  const [cuestionario, setCuestionario] = useState<CuestionarioDTO | null>(null);
  const [bloque, setBloque] = useState<WizardDTO | null>(null);
  const [elegidas, setElegidas] = useState<Record<number, number>>({});
  const [errores, setErrores] = useState<string[]>([]);
  const [enviando, setEnviando] = useState(false);
  const [respondidas, setRespondidas] = useState(0);
  // Cada ingreso cuenta un acceso, así que la carga se hace una sola vez
  const yaCargo = useRef(false);

  const cargar = useCallback(async () => {
    const id = cuestionarioActual();
    if (!id) {
      router.replace('/');
      return;
    }
    try {
      const datos = await api.get<CuestionarioDTO>(`/cuestionarios/${id}`);
      setCuestionario(datos);
      if (ESTADOS_VENCIDOS.includes(datos.estado)) {
        setFase('vencido');
        return;
      }
      if (datos.estado === 'EN_PROCESO') {
        // 1.C: ya había ingresado, se saltean las instrucciones
        await comenzar(id);
        return;
      }
      setFase('instrucciones');
    } catch (e) {
      setErrores(mensajesDeError(e));
      setFase('vencido');
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [router]);

  useEffect(() => {
    if (yaCargo.current) {
      return;
    }
    yaCargo.current = true;
    cargar();
  }, [cargar]);

  async function comenzar(id: number) {
    setErrores([]);
    try {
      const datos = await api.post<WizardDTO>(`/cuestionarios/${id}/comenzar`);
      if (datos.nroBloqueActual === 0) {
        setFase('fin');
        return;
      }
      setBloque(datos);
      setElegidas({});
      setFase('bloque');
    } catch (e) {
      setErrores(mensajesDeError(e));
      setFase('vencido');
    }
  }

  async function enviarBloque() {
    if (!bloque) {
      return;
    }
    setErrores([]);
    setEnviando(true);
    try {
      const datos = await api.post<WizardDTO>(
        `/cuestionarios/${bloque.idCuestionario}/bloques/${bloque.nroBloqueActual}`,
        {
          respuestas: Object.entries(elegidas).map(([idPregunta, idOpcion]) => ({
            idPreguntaCuestionario: Number(idPregunta),
            idItemOpcionElegido: idOpcion,
          })),
        },
      );
      setRespondidas(respondidas + bloque.preguntas.length);
      if (datos.nroBloqueActual === 0) {
        setFase('fin');
        return;
      }
      setBloque(datos);
      setElegidas({});
    } catch (e) {
      // 6.A: se mantiene el bloque y lo que ya estaba respondido
      setErrores(mensajesDeError(e));
    } finally {
      setEnviando(false);
    }
  }

  function salir() {
    cerrarSesion();
    router.push('/');
  }

  const encabezado = (
    <header className="hdr">
      <span className="hdr-logo">
        <span className="hdr-logo-mark">EP</span>
        <span className="hdr-logo-text">
          Eval<span>Pro</span>
        </span>
      </span>
      <div className="hdr-divider" />
      <div className="hdr-breadcrumb">
        <span className="cur">Cuestionario de Evaluación</span>
      </div>
      {cuestionario && (
        <div className="hdr-user">
          <span className="hdr-avatar">
            {cuestionario.nombreCandidato.slice(0, 2).toUpperCase()}
          </span>
          <span>{cuestionario.nombreCandidato}</span>
        </div>
      )}
    </header>
  );

  if (fase === 'cargando') {
    return (
      <div className="pantalla">
        {encabezado}
        <div className="cargando">Cargando su cuestionario…</div>
      </div>
    );
  }

  if (fase === 'vencido') {
    const mensaje =
      cuestionario?.estado === 'COMPLETO'
        ? 'Su cuestionario ya fue completado. ¡Gracias!'
        : cuestionario?.estado === 'SIN_CONTESTAR'
          ? 'El cuestionario venció: pasó el plazo para comenzarlo.'
          : 'El cuestionario venció: pasó el tiempo máximo o la cantidad de accesos permitidos.';
    return (
      <div className="pantalla">
        {encabezado}
        <div className="content" style={{ alignItems: 'center', justifyContent: 'center' }}>
          <Panel titulo="Cuestionario" estilo={{ maxWidth: 760, width: '100%' }}>
            <Mensaje tipo={cuestionario?.estado === 'COMPLETO' ? 'success' : 'warn'} estilo={{ marginTop: 0 }}>
              {mensaje}
            </Mensaje>
            <ListaErrores errores={errores} />
            <div className="btn-row" style={{ marginTop: 16 }}>
              <Boton tipo="primary" onClick={salir}>
                ✔ Aceptar
              </Boton>
            </div>
          </Panel>
        </div>
      </div>
    );
  }

  if (fase === 'instrucciones' && cuestionario) {
    return (
      <div className="pantalla">
        {encabezado}
        <div className="content" style={{ alignItems: 'center', justifyContent: 'center' }}>
          <section className="panel" style={{ maxWidth: 900, width: '100%' }}>
            <div className="panel-head">Instrucciones del Cuestionario</div>
            <div className="panel-body" style={{ lineHeight: 1.6 }}>
              <p style={{ marginBottom: 14 }}>
                Bienvenido/a al cuestionario de evaluación de competencias. Por favor, lea
                atentamente las siguientes instrucciones antes de comenzar:
              </p>
              <p style={{ marginBottom: 14 }}>{cuestionario.instrucciones}</p>
              <Mensaje tipo="info">
                Tiene un tiempo máximo de <strong>{cuestionario.tiempoRestante} minutos</strong>{' '}
                desde que comience el cuestionario.
              </Mensaje>
              <ListaErrores errores={errores} onCerrar={() => setErrores([])} />
            </div>
            <div className="action-bar">
              <Boton onClick={salir}>✕ Cancelar</Boton>
              <Boton
                tipo="primary"
                onClick={() => comenzar(cuestionario.idCuestionario)}
                estilo={{ marginLeft: 'auto' }}
              >
                Comenzar →
              </Boton>
            </div>
          </section>
        </div>
      </div>
    );
  }

  if (fase === 'bloque' && bloque) {
    const contestadas = Object.keys(elegidas).length;
    const total = bloque.preguntas.length;
    const ultimo = bloque.nroBloqueActual === bloque.cantidadBloquesTotales;
    return (
      <div className="pantalla">
        {encabezado}
        <div className="content" style={{ alignItems: 'center' }}>
          <div style={{ width: '100%', maxWidth: 900 }}>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                marginBottom: 8,
                flexWrap: 'wrap',
                gap: 8,
              }}
            >
              <span style={{ color: 'var(--mid)' }}>
                {contestadas} de {total} respondidas en este bloque
              </span>
              <span style={{ color: 'var(--primary)', fontWeight: 'bold' }}>
                Bloque {bloque.nroBloqueActual} de {bloque.cantidadBloquesTotales}
              </span>
            </div>
            <div className="progress-wrap" style={{ marginBottom: 16 }}>
              <div
                className="progress-fill"
                style={{ width: `${total === 0 ? 0 : (contestadas / total) * 100}%` }}
              />
            </div>

            <ListaErrores
              errores={errores}
              titulo="Debe responder todas las preguntas antes de continuar:"
              onCerrar={() => setErrores([])}
            />

            {bloque.preguntas.map((pregunta, i) => (
              <section className="panel" key={pregunta.idPreguntaCuestionario} style={{ marginBottom: 14 }}>
                <div className="panel-head">Pregunta {i + 1}</div>
                <div className="panel-body">
                  <div style={{ fontSize: 17, fontWeight: 'bold', marginBottom: 14, lineHeight: 1.4 }}>
                    {pregunta.enunciado}
                  </div>
                  {pregunta.opciones.map((opcion) => {
                    const marcada = elegidas[pregunta.idPreguntaCuestionario] === opcion.idOpcion;
                    return (
                      <button
                        key={opcion.idOpcion}
                        className={`q-option${marcada ? ' selected' : ''}`}
                        onClick={() =>
                          setElegidas({
                            ...elegidas,
                            [pregunta.idPreguntaCuestionario]: opcion.idOpcion,
                          })
                        }
                      >
                        <span className={`radio-circle${marcada ? ' checked' : ''}`} />
                        <span>{opcion.descripcion}</span>
                      </button>
                    );
                  })}
                </div>
              </section>
            ))}

            <div className="btn-row" style={{ marginBottom: 24 }}>
              <Boton
                tipo={ultimo ? 'success' : 'primary'}
                onClick={enviarBloque}
                deshabilitado={enviando}
                estilo={{ marginLeft: 'auto' }}
              >
                {ultimo ? '✔ Finalizar' : 'Siguiente →'}
              </Boton>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="pantalla">
      {encabezado}
      <div className="content" style={{ alignItems: 'center', justifyContent: 'center' }}>
        <div className="modal-box" style={{ textAlign: 'center', maxWidth: 680 }}>
          <div style={{ fontSize: 48, marginBottom: 8 }}>🎉</div>
          <div className="modal-title" style={{ border: 'none', color: 'var(--success)' }}>
            ¡Cuestionario completado con éxito!
          </div>
          <div className="modal-body">
            Sus respuestas quedaron registradas. Ya puede cerrar esta ventana.
          </div>
          <div className="modal-actions" style={{ justifyContent: 'center' }}>
            <Boton tipo="success" onClick={salir}>
              ✔ Aceptar
            </Boton>
          </div>
        </div>
      </div>
    </div>
  );
}
