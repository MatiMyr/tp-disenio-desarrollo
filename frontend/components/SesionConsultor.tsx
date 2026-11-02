'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';

import { consultorActual } from '@/lib/sesion';

/**
 * Las pantallas del consultor la usan para saber quién está logueado. Si no hay
 * sesión, vuelve a la pantalla de ingreso.
 */
export function useConsultor(): string | null {
  const router = useRouter();
  const [usuario, setUsuario] = useState<string | null>(null);

  useEffect(() => {
    const actual = consultorActual();
    if (!actual) {
      router.replace('/');
      return;
    }
    setUsuario(actual);
  }, [router]);

  return usuario;
}
