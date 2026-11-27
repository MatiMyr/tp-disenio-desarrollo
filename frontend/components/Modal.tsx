'use client';

type Props = {
  titulo: React.ReactNode;
  children: React.ReactNode;
  acciones: React.ReactNode;
  colorTitulo?: string;
  centrado?: boolean;
  anchoMaximo?: number;
};

export default function Modal({
  titulo,
  children,
  acciones,
  colorTitulo,
  centrado,
  anchoMaximo,
}: Props) {
  return (
    <div className="modal-overlay">
      <div
        className="modal-box"
        style={{ textAlign: centrado ? 'center' : undefined, maxWidth: anchoMaximo }}
      >
        <div className="modal-title" style={{ color: colorTitulo }}>
          {titulo}
        </div>
        <div className="modal-body">{children}</div>
        <div
          className="modal-actions"
          style={{ justifyContent: centrado ? 'center' : undefined }}
        >
          {acciones}
        </div>
      </div>
    </div>
  );
}
