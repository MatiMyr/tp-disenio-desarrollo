/** @type {import('next').NextConfig} */
const nextConfig = {
  // En desarrollo el frontend redirige /api a Spring Boot.
  // En produccion lo hace Nginx, como en el diagrama de despliegue.
  async rewrites() {
    return [
      {
        source: '/api/:ruta*',
        destination: 'http://localhost:8080/api/:ruta*',
      },
    ];
  },
};

module.exports = nextConfig;
