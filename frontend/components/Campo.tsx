'use client';

type Base = {
  etiqueta: string;
  obligatorio?: boolean;
  opcional?: boolean;
  ancho?: 'narrow' | 'wide' | 'full';
  error?: boolean;
};

type PropsTexto = Base & {
  valor: string;
  onChange: (valor: string) => void;
  marcador?: string;
  tipo?: 'text' | 'password';
  maxLargo?: number;
  deshabilitado?: boolean;
  onEnter?: () => void;
};

function clases(ancho?: string) {
  return `field${ancho ? ` ${ancho}` : ''}`;
}

function Etiqueta({ etiqueta, obligatorio, opcional }: Base) {
  return (
    <span className="field-label">
      {etiqueta}
      {obligatorio && <span className="req-mark">*</span>}
      {opcional && <span className="opt-label-mark">(opcional)</span>}
    </span>
  );
}

/** Campo de texto. */
export function CampoTexto(props: PropsTexto) {
  const { valor, onChange, marcador, tipo, maxLargo, deshabilitado, error, ancho, onEnter } = props;
  return (
    <div className={clases(ancho)}>
      <Etiqueta {...props} />
      <input
        className={`field-input${valor ? ' filled' : ''}${error ? ' error' : ''}`}
        type={tipo ?? 'text'}
        value={valor}
        maxLength={maxLargo}
        placeholder={marcador}
        disabled={deshabilitado}
        autoComplete="off"
        spellCheck={false}
        onChange={(e) => onChange(e.target.value)}
        onKeyDown={(e) => {
          if (e.key === 'Enter' && onEnter) {
            e.preventDefault();
            onEnter();
          }
        }}
      />
    </div>
  );
}

type Opcion = { valor: string; texto: string };

type PropsSelect = Base & {
  valor: string;
  onChange: (valor: string) => void;
  opciones: Opcion[];
  marcador?: string;
  deshabilitado?: boolean;
  marcadorDeshabilitado?: string;
};

/** Combo de selección. */
export function CampoSelect(props: PropsSelect) {
  const {
    valor,
    onChange,
    opciones,
    marcador,
    deshabilitado,
    marcadorDeshabilitado,
    error,
    ancho,
  } = props;
  return (
    <div className={clases(ancho)}>
      <Etiqueta {...props} />
      <select
        className={`field-select${valor ? ' filled' : ''}${error ? ' error' : ''}`}
        value={valor}
        disabled={deshabilitado}
        onChange={(e) => onChange(e.target.value)}
      >
        <option value="">
          {deshabilitado && marcadorDeshabilitado ? marcadorDeshabilitado : marcador ?? '— Seleccione —'}
        </option>
        {opciones.map((o) => (
          <option key={o.valor} value={o.valor}>
            {o.texto}
          </option>
        ))}
      </select>
    </div>
  );
}

type PropsArea = Base & {
  valor: string;
  onChange: (valor: string) => void;
  marcador?: string;
  maxLargo?: number;
  filas?: number;
};

/** Campo de texto de varias líneas. */
export function CampoArea(props: PropsArea) {
  const { valor, onChange, marcador, maxLargo, filas, error, ancho } = props;
  return (
    <div className={clases(ancho)}>
      <Etiqueta {...props} />
      <textarea
        className={`field-textarea${valor ? ' filled' : ''}${error ? ' error' : ''}`}
        value={valor}
        maxLength={maxLargo}
        placeholder={marcador}
        rows={filas ?? 3}
        spellCheck={false}
        onChange={(e) => onChange(e.target.value)}
      />
    </div>
  );
}
