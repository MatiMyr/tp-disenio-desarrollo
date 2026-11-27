'use client';

import { useRouter } from 'next/navigation';

import Boton from '@/components/Boton';
import Encabezado from '@/components/Encabezado';
import { useConsultor } from '@/components/SesionConsultor';
import { cerrarSesion, iniciales } from '@/lib/sesion';

type Acceso = {
  cu: string;
  titulo: string;
  ruta?: string;
};

type Columna = {
  numero: string;
  titulo: string;
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
    accesos: [
      { cu: 'CU008', titulo: 'Gestionar Puestos' },
      { cu: 'CU009', titulo: 'Dar de Alta Puesto' },
      { cu: 'CU020', titulo: 'Gestionar Preguntas', ruta: '/preguntas' },
      { cu: 'CU021', titulo: 'Dar de Alta Pregunta', ruta: '/preguntas/nueva' },
    ],
  },
  {
    numero: '2',
    titulo: 'Evaluación',
    accesos: [{ cu: 'CU025', titulo: 'Evaluar a Candidatos', ruta: '/evaluaciones/nueva' }],
  },
  {
    numero: '3',
    titulo: 'Reportes',
    accesos: [
      { cu: 'CU027', titulo: 'Reporte Comparativo' },
      { cu: 'CU028', titulo: 'Emitir Orden de Mérito', ruta: '/orden-merito' },
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
          <h1>Sistema de Gestión de Evaluaciones de Perfiles</h1>
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
                      <span className="cu-txt cu-title">{acceso.titulo}</span>
                      <span className="cu-go">→</span>
                    </button>
                  ))}
                </div>
              </section>
            </span>
          ))}
        </div>

      </div>
    </div>
  );
}
