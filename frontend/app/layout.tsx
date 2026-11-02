import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'EvalPro - Gestión de Evaluaciones de Perfiles',
  description: 'Sistema de gestión de evaluaciones de perfiles de Capit@l Humano',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="es">
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link
          href="https://fonts.googleapis.com/css2?family=Patrick+Hand&display=swap"
          rel="stylesheet"
        />
      </head>
      <body>{children}</body>
    </html>
  );
}
