'use client';

type Props = {
  pasos: string[];
  /** Paso actual, empezando en 1. */
  actual: number;
};

/** Barra de pasos del asistente, como el wizard del prototipo. */
export default function AsistentePasos({ pasos, actual }: Props) {
  return (
    <div className="wizard-bar">
      {pasos.map((paso, i) => {
        const numero = i + 1;
        const estado = numero < actual ? 'done' : numero === actual ? 'active' : '';
        return (
          <span key={paso} style={{ display: 'flex', alignItems: 'center' }}>
            {i > 0 && <div className={`wizard-connector${numero <= actual ? ' done' : ''}`} />}
            <div className={`wizard-step ${estado}`}>
              <div className="wizard-num">{numero < actual ? '✔' : numero}</div>
              <span>{paso}</span>
            </div>
          </span>
        );
      })}
    </div>
  );
}
