type Props = {
  titulo?: React.ReactNode;
  children: React.ReactNode;
  /** El panel crece para ocupar el alto que queda (grillas). */
  crece?: boolean;
  ajustado?: boolean;
  pie?: React.ReactNode;
  estilo?: React.CSSProperties;
};

export default function Panel({ titulo, children, crece, ajustado, pie, estilo }: Props) {
  return (
    <section className={`panel${crece ? ' grow' : ''}`} style={estilo}>
      {titulo !== undefined && <div className="panel-head">{titulo}</div>}
      <div className={`panel-body${ajustado ? ' tight' : ''}`}>{children}</div>
      {pie && <div className="action-bar">{pie}</div>}
    </section>
  );
}
