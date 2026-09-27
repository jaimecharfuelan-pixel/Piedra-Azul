/* Mermaid: diagramas grandes (clase) necesitan más margen de texto/aristas. */
document$.subscribe(() => {
  if (typeof mermaid === "undefined") {
    return;
  }
  mermaid.initialize({
    startOnLoad: true,
    securityLevel: "loose",
    maxTextSize: 120000,
    maxEdges: 2500,
    theme: document.body.getAttribute("data-md-color-scheme") === "slate" ? "dark" : "default",
  });
});
