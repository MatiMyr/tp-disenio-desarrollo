'use client';

import { useEffect, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';

import { cerrarSesion } from '@/lib/sesion';

export type Miga = { texto: string; ruta?: string };

type Props = {
  migas: Miga[];
  usuario?: string;
  inicialesUsuario?: string;
};

/** Encabezado con el logo, las migas de pan y el menú del usuario. */
export default function Encabezado({ migas, usuario, inicialesUsuario }: Props) {
  const router = useRouter();
  const [abierto, setAbierto] = useState(false);
  const menu = useRef<HTMLDivElement>(null);

  // Al tocar fuera del menú, se cierra
  useEffect(() => {
    function alTocarFuera(e: MouseEvent) {
      if (menu.current && !menu.current.contains(e.target as Node)) {
        setAbierto(false);
      }
    }
    document.addEventListener('mousedown', alTocarFuera);
    return () => document.removeEventListener('mousedown', alTocarFuera);
  }, []);

  return (
    <header className="hdr">
      <button className="hdr-logo" onClick={() => router.push('/inicio')} title="Ir al inicio">
        <span className="hdr-logo-mark">EP</span>
        <span className="hdr-logo-text">
          Eval<span>Pro</span>
        </span>
      </button>
      <div className="hdr-divider" />
      <nav className="hdr-breadcrumb">
        {migas.map((miga, i) => {
          const ultima = i === migas.length - 1;
          return (
            <span key={`${miga.texto}-${i}`} style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              {i > 0 && <span>›</span>}
              {ultima ? (
                <span className="cur">{miga.texto}</span>
              ) : miga.ruta ? (
                <button className="crumb-link" onClick={() => router.push(miga.ruta!)}>
                  {miga.texto}
                </button>
              ) : (
                <span>{miga.texto}</span>
              )}
            </span>
          );
        })}
      </nav>
      {usuario && (
        <div className="hdr-menu" ref={menu}>
          <button
            className="hdr-user"
            onClick={() => setAbierto(!abierto)}
            aria-expanded={abierto}
            title="Opciones de la sesión"
          >
            <span className="hdr-avatar">
              {inicialesUsuario ?? usuario.slice(0, 2).toUpperCase()}
            </span>
            <span>{usuario}</span>
            <span className="hdr-flecha">{abierto ? '▴' : '▾'}</span>
          </button>
          {abierto && (
            <div className="hdr-desplegable">
              <button
                onClick={() => {
                  cerrarSesion();
                  router.push('/');
                }}
              >
                ⎋ Cerrar sesión
              </button>
            </div>
          )}
        </div>
      )}
    </header>
  );
}
