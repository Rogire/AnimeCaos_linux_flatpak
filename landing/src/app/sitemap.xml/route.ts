import { SITE_URL } from "@/lib/seo";

const LOCALES = ["pt", "en"] as const;
const ROUTES = ["", "/about", "/download", "/how-to-use", "/contact"] as const;

function buildXmlSitemap() {
  const lastmod = new Date().toISOString();

  const urls = LOCALES.flatMap((locale) =>
    ROUTES.map((route) => {
      const url = `${SITE_URL}/${locale}${route}`;
      const changefreq = route === "" ? "weekly" : "monthly";
      const priority = route === "" ? "1.0" : "0.8";

      return [
        "  <url>",
        `    <loc>${url}</loc>`,
        `    <lastmod>${lastmod}</lastmod>`,
        `    <changefreq>${changefreq}</changefreq>`,
        `    <priority>${priority}</priority>`,
        "  </url>",
      ].join("\n");
    }),
  ).join("\n");

  return [
    '<?xml version="1.0" encoding="UTF-8"?>',
    '<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">',
    urls,
    "</urlset>",
  ].join("\n");
}

export async function GET() {
  return new Response(buildXmlSitemap(), {
    headers: {
      "Content-Type": "application/xml; charset=utf-8",
      "Cache-Control": "public, max-age=0, must-revalidate",
    },
  });
}
