'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';

import Boton from '@/components/Boton';
import { CampoSelect, CampoTexto } from '@/components/Campo';
import { ListaErrores } from '@/components/Mensaje';
import { api, mensajesDeError } from '@/lib/api';
import { guardarConsultor, guardarCuestionario } from '@/lib/sesion';

type Rol = 'consultor' | 'candidato';

const TIPOS_DOCUMENTO = [
  { valor: 'DNI', texto: 'DNI' },
  { valor: 'LC', texto: 'LC' },
  { valor: 'LE', texto: 'LE' },
  { valor: 'PP', texto: 'Pasaporte' },
];

/** CU001: pantalla de ingreso. Primero se elige el tipo de acceso. */
export default function PantallaIngreso() {
  const router = useRouter();
  const [rol, setRol] = useState<Rol>('consultor');
  const [usuario, setUsuario] = useState('');
  const [contrasena, setContrasena] = useState('');
  const [tipoDocumento, setTipoDocumento] = useState('DNI');
  const [nroDocumento, setNroDocumento] = useState('');
  const [clave, setClave] = useState('');
  const [errores, setErrores] = useState<string[]>([]);
  const [enviando, setEnviando] = useState(false);

  function cambiarRol(nuevo: Rol) {
    setRol(nuevo);
    setErrores([]);
  }

  async function ingresar() {
    setErrores([]);
    setEnviando(true);
    try {
      if (rol === 'consultor') {
        const datos = await api.post<{ nombreUsuario: string }>('/auth/consultor', {
          usuario,
          contrasena,
        });
        guardarConsultor(datos.nombreUsuario);
        router.push('/inicio');
      } else {
        const datos = await api.post<{ idCuestionario: number }>('/auth/candidato', {
          tipoDocumento,
          nroDocumento,
          clave,
        });
        guardarCuestionario(datos.idCuestionario);
        router.push('/cuestionario');
      }
    } catch (e) {
      // El sistema blanquea los campos sensibles y deja los errores a la vista
      setErrores(mensajesDeError(e));
      setContrasena('');
      setClave('');
    } finally {
      setEnviando(false);
    }
  }

  const esCandidato = rol === 'candidato';

  return (
    <div className="pantalla">
      <header className="hdr">
        <span className="hdr-logo">
          <span className="hdr-logo-mark">EP</span>
          <span className="hdr-logo-text">
            Eval<span>Pro</span>
          </span>
        </span>
        <div className="hdr-divider" />
        <div className="hdr-breadcrumb">
          <span className="cur">Ingreso al sistema</span>
        </div>
      </header>

      <div className="content row" style={{ gap: 40, alignItems: 'center' }}>
        <div style={{ flex: 1, minWidth: 320 }}>
          <h1 style={{ fontSize: 34, lineHeight: 1.1, marginBottom: 14 }}>
            Sistema de Gestión de
            <br />
            Evaluaciones de Perfiles
          </h1>
          <p style={{ fontSize: 17, color: '#555', lineHeight: 1.5, maxWidth: 620, marginBottom: 22 }}>
            Los <strong>consultores</strong> definen puestos, arman preguntas, evalúan candidatos y
            emiten reportes. Los <strong>candidatos</strong> ingresan con la clave que recibieron
            para completar su cuestionario.
          </p>
          <div className="login-steps">
            <div>
              <span className="flow-num">1</span>El consultor genera la evaluación (CU025)
            </div>
            <div>
              <span className="flow-num">2</span>El candidato recibe su clave de ingreso
            </div>
            <div>
              <span className="flow-num" style={{ background: 'var(--success)' }}>
                3
              </span>
              El candidato completa el cuestionario (CU026)
            </div>
          </div>
        </div>

        <div className="panel" style={{ width: 520, flexShrink: 0, maxWidth: '100%' }}>
          <div className="panel-head">Ingresar como</div>
          <div className="panel-body">
            <div className="role-row">
              <button
                className={`role-card${rol === 'consultor' ? ' on' : ''}`}
                onClick={() => cambiarRol('consultor')}
              >
                <span className={`radio-circle${rol === 'consultor' ? ' checked' : ''}`} />
                <span className="role-title">Consultor</span>
              </button>
              <button
                className={`role-card${esCandidato ? ' on' : ''}`}
                onClick={() => cambiarRol('candidato')}
              >
                <span className={`radio-circle${esCandidato ? ' checked' : ''}`} />
                <span className="role-title">Candidato</span>
              </button>
            </div>

            {esCandidato ? (
              <>
                <div className="form-row">
                  <CampoSelect
                    etiqueta="Tipo de documento"
                    obligatorio
                    ancho="narrow"
                    valor={tipoDocumento}
                    onChange={setTipoDocumento}
                    opciones={TIPOS_DOCUMENTO}
                  />
                  <CampoTexto
                    etiqueta="Número de documento"
                    obligatorio
                    valor={nroDocumento}
                    onChange={setNroDocumento}
                    marcador="Ej: 30000006"
                    onEnter={ingresar}
                  />
                </div>
                <div className="form-row">
                  <CampoTexto
                    etiqueta="Clave de ingreso"
                    obligatorio
                    ancho="full"
                    valor={clave}
                    onChange={setClave}
                    marcador="La clave que le envió el consultor"
                    onEnter={ingresar}
                  />
                </div>
              </>
            ) : (
              <>
                <div className="form-row">
                  <CampoTexto
                    etiqueta="Usuario"
                    obligatorio
                    ancho="full"
                    valor={usuario}
                    onChange={setUsuario}
                    onEnter={ingresar}
                  />
                </div>
                <div className="form-row">
                  <CampoTexto
                    etiqueta="Contraseña"
                    obligatorio
                    ancho="full"
                    tipo="password"
                    valor={contrasena}
                    onChange={setContrasena}
                    onEnter={ingresar}
                  />
                </div>
              </>
            )}

            <ListaErrores errores={errores} onCerrar={() => setErrores([])} />
          </div>
          <div className="action-bar">
            <Boton
              tipo={esCandidato ? 'success' : 'primary'}
              onClick={ingresar}
              deshabilitado={enviando}
              estilo={{ marginLeft: 'auto' }}
            >
              {esCandidato ? 'Ingresar al cuestionario →' : 'Ingresar →'}
            </Boton>
          </div>
        </div>
      </div>
    </div>
  );
}
