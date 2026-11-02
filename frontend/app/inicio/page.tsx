'use client';

import { useRouter } from 'next/navigation';

import Boton from '@/components/Boton';
import Encabezado from '@/components/Encabezado';
import { useConsultor } from '@/components/SesionConsultor';
import { cerrarSesion, iniciales } from '@/lib/sesion';

type Acceso = {
  cu: string;
  titulo: string;
  descripcion: string;
  ruta?: string;
};

type Columna = {
  numero: string;
  titulo: string;
  subtitulo: string;
  accesos: Acceso[];
};

/**
 * Inicio del consultor: el flujo en tres columnas del prototipo. Los casos de
 * uso que no entran en esta etapa se muestran deshabilitados.
 */
const COLUMNAS: Columna[] = [
  {
    numero: '1',
    titulo: 'Configuración',
    subtitulo: 'Definir qué se evalúa',
    accesos: [
      { cu: 'CU008', titulo: 'Gestionar Puestos', descripcion: 'Buscar, modificar y eliminar puestos' },
      { cu: 'CU009', titulo: 'Dar de Alta Puesto', descripcion: 'Datos del puesto y competencias' },
      {
        cu: 'CU020',
        titulo: 'Gestionar Preguntas',
        descripcion: 'Buscar por competencia y factor',
        ruta: '/preguntas',
      },
      {
        cu: 'CU021',
        titulo: 'Dar de Alta Pregunta',
        descripcion: 'Opciones y ponderaciones',
        ruta: '/preguntas/nueva',
      },
    ],
  },
  {
    numero: '2',
    titulo: 'Evaluación',
    subtitulo: 'Generar cuestionarios y claves',
    accesos: [
      {
        cu: 'CU025',
        titulo: 'Evaluar a Candidatos',
        descripcion: 'Candidatos, puesto, claves de ingreso y Excel',
        ruta: '/evaluaciones/nueva',
      },
    ],
  },
  {
    numero: '3',
    titulo: 'Reportes',
    subtitulo: 'Analizar los resultados',
    accesos: [
      { cu: 'CU027', titulo: 'Reporte Comparativo', descripcion: 'Requerido vs. obtenido' },
      {
        cu: 'CU028',
        titulo: 'Emitir Orden de Mérito',
        descripcion: 'Ranking de aptos y no aptos',
        ruta: '/orden-merito',
      },
    ],
  },
];

export default function PantallaInicio() {
  const router = useRouter();
  const usuario = useConsultor();

  if (!usuario) {
    return <div className="cargando">Cargando…</div>;
  }

  return (
    <div className="pantalla">
      <Encabezado migas={[{ texto: 'Inicio' }]} usuario={usuario} inicialesUsuario={iniciales(usuario)} />
      <div className="content" style={{ gap: 18 }}>
        <div className="home-top">
          <div>
            <h1>Sistema de Gestión de Evaluaciones de Perfiles</h1>
            <div className="sub">Consultora Capit@l Humano</div>
          </div>
          <div className="btn-row">
            <Boton
              chico
              onClick={() => {
                cerrarSesion();
                router.push('/');
              }}
            >
              ⎋ Cerrar sesión
            </Boton>
          </div>
        </div>

        <div className="flow">
          {COLUMNAS.map((columna, i) => (
            <span key={columna.numero} style={{ display: 'flex', flex: 1, minWidth: 260 }}>
              {i > 0 && <div className="flow-arrow">➜</div>}
              <section className="panel flow-col">
                <div className="panel-head flow-head">
                  <span className="flow-num">{columna.numero}</span>
                  <span>{columna.titulo}</span>
                  <span className="flow-sub">{columna.subtitulo}</span>
                </div>
                <div>
                  {columna.accesos.map((acceso) => (
                    <button
                      key={acceso.cu}
                      className="cu-item"
                      disabled={!acceso.ruta}
                      title={acceso.ruta ? undefined : 'No disponible en esta entrega'}
                      onClick={() => acceso.ruta && router.push(acceso.ruta)}
                    >
                      <span className="cu-badge">{acceso.cu}</span>
                      <span className="cu-txt">
                        <span className="cu-title" style={{ display: 'block' }}>
                          {acceso.titulo}
                        </span>
                        <span className="cu-desc" style={{ display: 'block' }}>
                          {acceso.descripcion}
                        </span>
                      </span>
                      <span className="cu-go">→</span>
                    </button>
                  ))}
                </div>
              </section>
            </span>
          ))}
        </div>

        <div className="home-legend">
          <span>
            El candidato completa su cuestionario (CU026) ingresando con su clave desde la pantalla
            de ingreso
          </span>
          <span style={{ marginLeft: 'auto' }}>
            Tocá cualquier caso de uso para abrirlo · el logo <strong>EvalPro</strong> siempre vuelve
            acá
          </span>
        </div>
      </div>
    </div>
  );
}
