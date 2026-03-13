# AnimeCaos Mobile - Plano de Implementacao UI/UX

## 1. Objetivo
Entregar uma interface mobile moderna, minimalista e profissional, com identidade propria e experiencia fluida, evitando visual generico.

Este plano sera executado em fases e atualizado continuamente.

---

## 2. Diretrizes Visuais Obrigatorias

### Tema e estilo
- Dark theme como base.
- Cor secundaria: vermelho.
- Linguagem visual: `Liquidglass` + `Frostedglass` + `Glassmorphism`.
- Referencia de acabamento: estilo macOS (profissional, limpo, refinado).

### Composicao e hierarquia
- Layout visual hierarquico, organizado e com leitura clara.
- Fluxo de experiencia simples, objetivo e fluido.
- UI profissional para uso real, sem excesso decorativo.

### Cores e superfícies
- Paleta reduzida e controlada.
- Sem excesso de cores ou gradientes.
- Gradientes apenas quando contribuirem para profundidade e foco.
- Superficies translúcidas com blur e contraste legivel.

### Iconografia e componentes
- Icones modernos e minimalistas.
- Componentes consistentes, com estados claros (default, hover/press, disabled, loading, error).
- Padrao visual unico para cards, listas, campos e CTAs.

### Motion e interacoes
- Animacoes suaves, premium e funcionais.
- Motion com duracao curta/media e easing natural.
- Evitar animacao gratuita; cada transicao deve reforcar contexto.

### Identidade e qualidade
- Proibido design generico de IA.
- UI/UX criativa, original e profissional.
- Foco em assinatura visual propria do produto.

---

## 3. Design System (Primeira Versao)

### 3.1 Tokens de design
- `color.bg.primary`: fundo principal dark.
- `color.bg.elevated`: fundo de cards/paineis glass.
- `color.brand.secondary`: vermelho (acao secundaria e destaque controlado).
- `color.text.primary`, `color.text.muted`, `color.text.onBrand`.
- `color.border.soft`, `color.border.focus`.
- `effect.blur.surface`, `effect.glass.opacity`, `effect.glow.soft`.
- `radius.sm/md/lg/xl` (priorizar cantos arredondados consistentes).
- `space.4/8/12/16/24/32`.
- `motion.fast/medium/slow` com easing padrao.

### 3.2 Tipografia
- Escala clara de hierarquia: titulo, subtitulo, corpo, metadata.
- Contraste alto para legibilidade no dark theme.
- Evitar excesso de pesos/familias; foco em consistencia.

### 3.3 Componentes-base
- AppShell (header + content + bottom nav quando necessario).
- SearchBar glass.
- AnimeCard (thumbnail, titulo, metadata, estado de foco).
- EpisodeListItem (indice, status, acao primaria).
- PrimaryAction / SecondaryAction.
- Feedback components: loading skeleton, empty state, error state, toast/snackbar.

---

## 4. Arquitetura de Experiencia (UX Flow)

### Fluxo principal
1. Health check backend rapido e transparente.
2. Busca de anime com feedback imediato.
3. Lista de resultados com hierarquia clara.
4. Selecao de anime e carregamento de episodios.
5. Reproducao com controles essenciais.
6. Proximo passo natural: continuar episodio, proximo episodio, ou favoritar.

### Regras UX
- Zero ambiguidade de acao principal por tela.
- Respostas visuais instantaneas em clique/toque.
- Estado de erro com texto claro e CTA de retry.
- Tempo de espera sempre acompanhado por loading contextual.

---

## 5. Backlog de Implementacao (Execucao)

## Fase 0 - Preparacao
- Consolidar este plano.
- Definir checklist de aceite por tela.
- Mapear `system_design` para requisitos de tela e componentes.

## Fase 1 - Fundacao Visual
- Implementar tokens (cores, espacos, raio, motion, glass effects).
- Criar tema dark oficial com vermelho secundario.
- Definir icon set minimalista consistente.
- Publicar guia rapido de uso de componentes.

## Fase 2 - Componentes e Layout
- Construir componentes-base reutilizaveis.
- Aplicar glassmorphism com legibilidade garantida.
- Ajustar hierarquia visual da home (busca, status, resultados).
- Garantir comportamento responsivo em telas comuns de Android.

## Fase 3 - Fluxo Funcional Completo
- Integrar estados reais da API em cada etapa.
- Melhorar estados de loading/error/empty/success.
- Refinar navegacao e continuidade de contexto.
- Padronizar feedback textual e visual.

## Fase 4 - Motion e Premium Finish
- Adicionar transicoes suaves entre blocos de interface.
- Implementar microinteracoes de toque e foco.
- Otimizar performance de animacao (sem jank).
- Remover qualquer elemento visual redundante.

## Fase 5 - Qualidade e Release
- Checklist final de UX/UI.
- Revisao de acessibilidade (contraste, tamanho de toque, legibilidade).
- QA funcional do fluxo completo.
- Hardening para release interna.

---

## 5.1 System Design Recebido (2026-03-12)

### Direcao visual obrigatoria
- Dark theme only.
- Secondary color: deep red.
- Aesthetic macOS-inspired.
- Liquid glass + frosted glass + glassmorphism.
- Transparencia sutil e reflexos leves.
- Minimalista, elegante e profissional.
- Paleta limitada, sem excesso de gradientes.
- Nao parecer layout generico de IA.

### Principios de UX obrigatorios
- Simples, rapido, objetivo e fluido.
- Sem passos desnecessarios.
- Interacoes profissionais e previsiveis.
- Evitar clutter, acentos de cor demais e sombras pesadas.

### Telas core obrigatorias
1. Splash (logo minimalista com glow sutil).
2. Home (Continue watching, Trending, Recently updated, Recommended).
3. Search (barra grande + grid de posters).
4. Anime details (header de poster + metadata glass + episodios).
5. Episode player (foco no video + controles transparentes).
6. Library (watchlist + continue watching).

### Estilo de componentes obrigatorio
- Cards glassmorphism com blur/frosted e cantos suaves.
- Botoes com acento vermelho e press/hover suave.
- Icones outline, modernos e consistentes.
- Microinteracoes premium (fade/scale/reflexo suave).

---

## 5.2 Status de Execucao Atual

- Fase 0: concluida.
- Fase 1: em execucao (tema dark premium + tokens implementados).
- Fase 2: em execucao (estrutura das 6 telas e componentes glass implementada no Android).
- Fase 3: iniciado parcialmente (integracao search/episodes/player via ViewModel).
- Fases 4 e 5: pendentes.

---

## 6. Criterios de Aceite

### Visual
- Dark theme aplicado em 100% das telas.
- Vermelho como secundaria de forma controlada e consistente.
- Superficies glass/frosted sem comprometer leitura.
- Interface com hierarquia clara e aparencia profissional.

### UX
- Fluxo principal completo sem bloqueios.
- Navegacao simples e objetiva.
- Erros e timeouts com mensagens claras e acao de recuperacao.
- Sem sobrecarga visual.

### Motion
- Animacoes suaves e discretas.
- Sem transicoes abruptas.
- Sem lag perceptivel em dispositivos-alvo.

### Qualidade de design
- Nao parecer template generico.
- Coerencia visual entre telas e estados.
- Assinatura propria de produto.

---

## 7. Proximos Passos de Implementacao
1. Consolidar refinamento visual premium por tela (spacing, tipografia, densidade e contraste).
2. Ligar dados reais de poster/sinopse no Details/Home/Search.
3. Integrar player nativo Android (Media3/ExoPlayer) no Episode Player.
4. Evoluir Library para persistencia real (watchlist e continue watching locais).
5. Rodar QA visual/funcional e ajustar motion para consistencia final.

---

## 8. Proxima Acao Imediata
Executar refinamento visual da Fase 2 e iniciar integracao de metadata/posters reais na Fase 3.
