'use client';

export type Columna = {
  titulo: string;
  ancho?: string;
  centrada?: boolean;
};

type Props = {
  columnas: Columna[];
  /** Filas ya armadas por la pantalla (cada una con sus celdas). */
  children: React.ReactNode;
  /** Cantidad de filas: si es 0 se muestra el texto vacío. */
  filas: number;
  textoVacio: string;
  /** Mensaje cuando la búsqueda no trajo resultados. */
  textoSinResultados?: string;
  buscado?: boolean;
  estilo?: React.CSSProperties;
};

export default function Grilla({
  columnas,
  children,
  filas,
  textoVacio,
  textoSinResultados,
  buscado,
  estilo,
}: Props) {
  let cuerpo: React.ReactNode = children;
  if (filas === 0) {
    const texto = buscado && textoSinResultados ? textoSinResultados : textoVacio;
    cuerpo = (
      <tr>
        <td className="empty-cell" colSpan={columnas.length}>
          {texto}
        </td>
      </tr>
    );
  }
  return (
    <div className="tbl-wrap" style={estilo}>
      <table>
        <thead>
          <tr>
            {columnas.map((c, i) => (
              <th
                key={`${c.titulo}-${i}`}
                style={{ width: c.ancho, textAlign: c.centrada ? 'center' : 'left' }}
              >
                {c.titulo}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>{cuerpo}</tbody>
      </table>
    </div>
  );
}
