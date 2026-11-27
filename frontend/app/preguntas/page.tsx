'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';

import Boton from '@/components/Boton';
import { CampoSelect, CampoTexto } from '@/components/Campo';
import Encabezado from '@/components/Encabezado';
import Grilla from '@/components/Grilla';
import Mensaje, { ListaErrores } from '@/components/Mensaje';
import Panel from '@/components/Panel';
import { useConsultor } from '@/components/SesionConsultor';
import { api, mensajesDeError } from '@/lib/api';
import { iniciales } from '@/lib/sesion';

type CompetenciaDTO = { codigo: string; nombre: string };
type FactorDTO = { codigo: string; nombre: string; nombreCompetencia: string };
type PreguntaDTO = {
  codigo: string;
  nombreCompetencia: string;
  nombreFactor: string;
  nombrePregunta: string;
};

/** CU020: buscar preguntas por competencia, factor y nombre. */
export default function PantallaGestionPreguntas() {
  const router = useRouter();
  const usuario = useConsultor();

  const [competencias, setCompetencias] = useState<CompetenciaDTO[]>([]);
  const [factores, setFactores] = useState<FactorDTO[]>([]);
  const [competencia, setCompetencia] = useState('');
  const [factor, setFactor] = useState('');
  const [nombre, setNombre] = useState('');

  const [resultados, setResultados] = useState<PreguntaDTO[]>([]);
  const [seleccionada, setSeleccionada] = useState<string | null>(null);
  const [buscado, setBuscado] = useState(false);
  const [errores, setErrores] = useState<string[]>([]);
  const [faltaCriterio, setFaltaCriterio] = useState(false);

  useEffect(() => {
    api
      .get<CompetenciaDTO[]>('/preguntas/competencias')
      .then(setCompetencias)
      .catch((e) => setErrores(mensajesDeError(e)));
  }, []);

  // Al cambiar la competencia se recargan los factores y se limpia el elegido
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

  async function buscar() {
    // La consigna pide datos en al menos uno de los criterios
    if (!competencia && !factor && !nombre.trim()) {
      setFaltaCriterio(true);
      setBuscado(false);
      setResultados([]);
      setSeleccionada(null);
      return;
    }
    setFaltaCriterio(false);
    setErrores([]);
    try {
      const parametros = new URLSearchParams();
      if (competencia) parametros.set('competencia', competencia);
      if (factor) parametros.set('factor', factor);
      if (nombre.trim()) parametros.set('nombre', nombre.trim());
      const datos = await api.get<PreguntaDTO[]>(`/preguntas?${parametros.toString()}`);
      setResultados(datos);
      setSeleccionada(null);
      setBuscado(true);
    } catch (e) {
      setErrores(mensajesDeError(e));
    }
  }

  if (!usuario) {
    return <div className="cargando">Cargando…</div>;
  }

  return (
    <div className="pantalla">
      <Encabezado
        migas={[{ texto: 'Inicio', ruta: '/inicio' }, { texto: 'Preguntas' }, { texto: 'Gestionar Preguntas' }]}
        usuario={usuario}
        inicialesUsuario={iniciales(usuario)}
      />
      <div className="content">
        <Panel titulo="Búsqueda de Preguntas">
          <div className="form-row">
            <CampoSelect
              etiqueta="Competencia"
              valor={competencia}
              onChange={setCompetencia}
              marcador="— Todas —"
              opciones={competencias.map((c) => ({ valor: c.codigo, texto: c.nombre }))}
            />
            <CampoSelect
              etiqueta="Factor"
              valor={factor}
              onChange={setFactor}
              marcador="— Todos —"
              deshabilitado={!competencia}
              marcadorDeshabilitado="— Primero seleccione competencia —"
              opciones={factores.map((f) => ({ valor: f.codigo, texto: f.nombre }))}
            />
            <CampoTexto
              etiqueta="Nombre de Pregunta"
              ancho="wide"
              valor={nombre}
              onChange={setNombre}
              onEnter={buscar}
            />
          </div>
          <div className="btn-row">
            <Boton tipo="primary" onClick={buscar}>
              🔍 Buscar
            </Boton>
            <Boton onClick={() => router.push('/inicio')} estilo={{ marginLeft: 'auto' }}>
              ✕ Salir
            </Boton>
          </div>
          {faltaCriterio && (
            <Mensaje tipo="error" onCerrar={() => setFaltaCriterio(false)}>
              Debe ingresar al menos un criterio de búsqueda
            </Mensaje>
          )}
          <ListaErrores errores={errores} onCerrar={() => setErrores([])} />
        </Panel>

        <section className="panel grow">
          <div className="panel-head">
            Resultados
            {buscado && (
              <span style={{ fontWeight: 'normal', textTransform: 'none', color: '#555' }}>
                {' '}
                — {resultados.length} pregunta(s) encontrada(s)
              </span>
            )}
          </div>
          <Grilla
            columnas={[
              { titulo: 'Competencia', ancho: '240px' },
              { titulo: 'Factor', ancho: '260px' },
              { titulo: 'Pregunta' },
            ]}
            filas={resultados.length}
            buscado={buscado}
            textoVacio="Ejecute una búsqueda para ver resultados"
            textoSinResultados="No se encontraron preguntas que coincidan con los criterios de búsqueda"
          >
            {resultados.map((p) => (
              <tr
                key={p.codigo}
                className={`clk${seleccionada === p.codigo ? ' selected' : ''}`}
                onClick={() => setSeleccionada(seleccionada === p.codigo ? null : p.codigo)}
              >
                <td>{p.nombreCompetencia}</td>
                <td>{p.nombreFactor}</td>
                <td>{p.nombrePregunta}</td>
              </tr>
            ))}
          </Grilla>
          <div className="action-bar">
            <Boton tipo="primary" onClick={() => router.push('/preguntas/nueva')}>
              + Nuevo
            </Boton>
            <Boton tipo="primary" deshabilitado titulo="CU022: no entra en esta entrega">
              ✎ Modificar
            </Boton>
            <Boton tipo="danger" deshabilitado titulo="CU023: no entra en esta entrega">
              ✕ Eliminar
            </Boton>
          </div>
        </section>
      </div>
    </div>
  );
}
