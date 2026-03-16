import type { Metadata } from "next";
import Script from "next/script";
import PageWrapper from "@/components/layout/PageWrapper";
import Hero from "@/components/sections/Hero";
import Features from "@/components/sections/Features";
import Stats from "@/components/sections/Stats";
import Screenshots from "@/components/sections/Screenshots";
import DownloadCTA from "@/components/sections/DownloadCTA";
import { buildLocalizedMetadata, toAppLocale } from "@/lib/seo";

export async function generateMetadata({ params }: { params: Promise<{ locale: string }> }): Promise<Metadata> {
    const locale = toAppLocale((await params).locale);
    const isPt = locale === "pt";

    return buildLocalizedMetadata({
        locale,
        title: isPt ? "AnimeCaos - Assistir Anime Desktop Sem Anúncios" : "AnimeCaos - Watch Anime Desktop Without Ads",
        description: isPt
            ? "AnimeCaos - O seu hub desktop open source para assistir animes sem anúncios, com player limpo, download offline e integração AniList."
            : "AnimeCaos - Open source desktop hub to watch anime without ads, with clean playback, offline downloads, and AniList integration.",
        keywords: isPt
            ? ["AnimeCaos", "assistir anime", "download anime", "anime player", "animes online", "animes grátis", "player de animes", "anime desktop"]
            : ["AnimeCaos", "watch anime", "anime download", "anime player", "free anime", "desktop anime player", "ad-free anime"],
    });
}

const getSitelinksJsonLd = (locale: string) => {
    const baseUrl = `https://animecaos.vercel.app/${locale}`;
    return {
        "@context": "https://schema.org",
        "@type": "ItemList",
        "itemListElement": [
            {
                "@type": "SiteNavigationElement",
                "position": 1,
                "name": locale === "pt" ? "Sobre" : "About",
                "url": `${baseUrl}/about`,
            },
            {
                "@type": "SiteNavigationElement",
                "position": 2,
                "name": "Download",
                "url": `${baseUrl}/download`,
            },
            {
                "@type": "SiteNavigationElement",
                "position": 3,
                "name": locale === "pt" ? "Como Usar" : "How to Use",
                "url": `${baseUrl}/how-to-use`,
            },
            {
                "@type": "SiteNavigationElement",
                "position": 4,
                "name": locale === "pt" ? "Contato" : "Contact",
                "url": `${baseUrl}/contact`,
            },
        ],
    };
};

export default async function HomePage({ params }: { params: Promise<{ locale: string }> }) {
    const { locale } = await params;

    return (
        <PageWrapper locale={locale}>
            <Script
                id="sitelinks-ld"
                type="application/ld+json"
                dangerouslySetInnerHTML={{ __html: JSON.stringify(getSitelinksJsonLd(locale)) }}
            />
            <Hero locale={locale} />
            <Features />
            <Stats />
            <Screenshots />
            <DownloadCTA />
        </PageWrapper>
    );
}
