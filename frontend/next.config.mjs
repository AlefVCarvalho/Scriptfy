// Endereço do backend (Spring Boot)
const API_URL = process.env.API_URL || "http://localhost:8080";

const nextConfig = {
  reactStrictMode: true,
  // O app chama /api/musicas, e aqui o "/api" é tirado antes de ir pro backend
  // (no backend o caminho é só /musicas)
  async rewrites() {
    return [{ source: "/api/:path*", destination: `${API_URL}/:path*` }];
  },
};

export default nextConfig;
