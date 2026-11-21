import "@fontsource-variable/bricolage-grotesque";
import "./style.css";

export const metadata = {
  title: "Scriptfy",
  description: "Adicione, edite, remova e toque suas músicas.",
};

export const viewport = {
  width: "device-width",
  initialScale: 1,
  themeColor: "#08080b",
};

export default function Layout({ children }) {
  return (
    <html lang="pt-BR">
      <body>{children}</body>
    </html>
  );
}
