'use client';

import { useRouter } from 'next/navigation';

export type Miga = { texto: string; ruta?: string };

type Props = {
  migas: Miga[];
  usuario?: string;
  inicialesUsuario?: string;
};

/** Encabezado con el logo, las migas de pan y el usuario, como en el prototipo. */
export default function Encabezado({ migas, usuario, inicialesUsuario }: Props) {
  const router = useRouter();

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
        <div className="hdr-user">
          <span className="hdr-avatar">{inicialesUsuario ?? usuario.slice(0, 2).toUpperCase()}</span>
          <span>{usuario}</span>
        </div>
      )}
    </header>
  );
}
