# Spec: Melhorias no Controle Financeiro Local (financehome)

> Especificação para o OpenCode implementar melhorias no app Android `Controle Financeiro Local`.
> Diretório do projeto: `/home/vitor/Documentos/wsgit/financehome/financeHome/`
> Spec original: ver `context.md` na raiz do projeto (27 seções).
> Stack: Kotlin + Jetpack Compose + Material 3 + Room (SQLite) + Koin.

---

## Contexto

Vitor pediu para melhorar o app Android de controle financeiro pessoal que está em `wsgit/financehome/financeHome/`. O app compila e tem 2 commits (`d00ae50c` Initial + `33efa5ac` Improve income type display), mas a implementação atual tem gaps em relação ao `context.md` e alguns bugs reais que vão frustrar o usuário em uso diário.

Objetivo desta spec: fechar os gaps mais importantes, corrigir bugs críticos e tornar o app usável de verdade. Não é uma reescrita: é uma evolução incremental.

---

## Decisões já tomadas (com números ①②③)

① **Sem mudança de stack.** Continua Kotlin + Compose + Room + Koin. Mantém arquitetura MVVM existente.

② **Sem backend, sem cloud, sem login.** Spec original já define offline-first. Esta spec mantém.

③ **APK debug é o entregável.** Após OpenCode terminar, Whatson (eu) roda `./gradlew assembleDebug` e envia o APK ao Vitor. Não há push automático.

④ **OpenCode commita ao final, sem push.** Conforme regra de combinação com Vitor (ver `~/.openclaw/workspace/AGENTS.md`).

⑤ **Idioma da UI: pt-BR.** Textos já estão em pt-BR, manter esse padrão.

⑥ **Tema visual: claro (light) por enquanto.** Dark mode fica para uma spec futura.

⑦ **Não criar nova entidade `Fatura`.** Spec original seção 11.6 diz que pode calcular dinamicamente. Manter assim.

---

## Bugs críticos a corrigir (fazer primeiro)

### B1. Duplicação de despesa recorrente

**Arquivo:** `app/src/main/java/com/vitor/controlefinanceiro/ui/screens/expenses/ExpenseViewModel.kt`

**Problema:** Quando o usuário marca o checkbox "Recorrente" e salva um gasto, o código cria:
- Um `RecurringExpenseEntity` (recorrência) ✅
- Um `ExpenseEntity` com `recurringExpenseId = null` e `recurringYear/Month = null` ❌

Quando o app reabre, `DashboardViewModel.init` chama `GenerateRecurringExpensesUseCase()`. O use case conta `recurringCount(recurringId, year, month)` — que retorna 0 porque o `ExpenseEntity` salvo não tem `recurringExpenseId` setado. Resultado: **o use case gera OUTRA despesa para o mês atual → duplicação visível no dashboard.**

**Correção:** No `ExpenseViewModel.save`, quando `form.recurring = true`:
- Criar a recorrência (gera um id)
- Criar o `ExpenseEntity` com `recurringExpenseId = recurring.id`, `recurringYear = hoje.year`, `recurringMonth = hoje.monthValue`, para que o contador retorne 1 e o use case não duplique

**Critério de aceitação:** Criar uma recorrência em maio/2026, reabrir o app, conferir que só existe 1 despesa em maio/2026 dessa recorrência.

### B2. Form de despesa sem data de compra nem vencimento

**Arquivos:**
- `app/src/main/java/com/vitor/controlefinanceiro/ui/screens/expenses/ExpenseListScreen.kt`
- `app/src/main/java/com/vitor/controlefinanceiro/ui/screens/expenses/ExpenseViewModel.kt`

**Problema:** Spec seção 11.4 exige campos `Data da compra` e `Data de vencimento` no form. O form atual só tem: nome, descrição, valor, categoria, forma de pagamento, cartão (condicional), status, recorrente (checkbox), observação. Hoje a data é fixada como `DateUtils.today()` no ViewModel — o usuário não consegue lançar gasto de ontem nem agendar vencimento futuro.

**Correção:**
- Adicionar `purchaseDateInput: String` e `dueDateInput: String?` ao `ExpenseFormState`
- Adicionar dois `LabeledTextField` no form: `Data da compra (dd/MM/yyyy)` (obrigatório, default = hoje) e `Data de vencimento (dd/MM/yyyy)` (opcional)
- Criar helper em `core/date/DateUtils.kt`: `fun parseBrDateOrNull(input: String): LocalDate?` que aceita `dd/MM/yyyy` e devolve `null` se inválido
- No ViewModel, parsear e validar. Se inválido, mostrar mensagem "Data invalida. Use dd/MM/yyyy."
- Para cartão de crédito, `dueDate` continua sendo sobrescrito pelo `normalizeCardFields` (regra de fatura). Para outras formas, o `dueDate` é o que o usuário digitou, ou `purchaseDate` se vazio.

**Critério de aceitação:** Salvar gasto em dinheiro com data de compra 15/06 e vencimento 20/06. Conferir que o dashboard de junho mostra o gasto.

### B3. Sem edição (UPDATE) de entidades

**Arquivos:**
- `ExpenseListScreen.kt` + `ExpenseViewModel.kt`
- `IncomeListScreen.kt` + `IncomeViewModel.kt`
- `CreditCardListScreen.kt` + `CreditCardViewModel.kt`
- `CategoryListScreen.kt` + `CategoryViewModel.kt`

**Problema:** Spec seções 11.2 (entradas), 11.3 (gastos), 11.4 (cartões), 11.7 (categorias) exigem criar **E** editar. Hoje só tem criar e excluir. Usuário não consegue corrigir typo no nome de uma categoria sem excluir e recriar (e categorias padrão são `isSystem = true`, então nem dá pra excluir).

**Correção:**
- Adicionar botão "Editar" em cada item da lista, ao lado de "Excluir" / "Marcar pago"
- No form de edição, pré-popular com os valores atuais
- ViewModel: novo método `update(entity)` que reaproveita o `repository.save` (que já usa `@Upsert`)

**Critério de aceitação:** Editar nome de categoria de "Outros" para "Outros gastos". Conferir que a mudança persiste após reabrir.

### B4. Dashboard com botões redundantes à bottom nav

**Arquivo:** `app/src/main/java/com/vitor/controlefinanceiro/ui/screens/dashboard/DashboardScreen.kt`

**Problema:** O dashboard tem 5 botões grandes: Adicionar entrada, Adicionar gasto, Cartões, Categorias, Backup. Esses 5 destinos já estão acessíveis via bottom navigation bar (`Inicio / Gastos / Entradas / Cartoes / Mais`). UX redundante, polui a tela.

**Correção:**
- Remover os 5 botões grandes do dashboard
- Adicionar um único `FloatingActionButton` "Adicionar" no dashboard que abre um pequeno menu (ou BottomSheet) com 2 opções: "Nova entrada" / "Novo gasto"
- Manter o resumo financeiro (cards de Entradas/Gastos/Saldo/Aberto/Pago/Cartão/Outros) ✅
- Adicionar lista "Últimos lançamentos" (5 itens: misto de entradas + gastos mais recentes) — spec seção 11.1 exige isso e tá faltando

**Critério de aceitação:** Abrir o dashboard, ver só os cards de resumo + lista de últimos 5 lançamentos + FAB "+". Bottom nav segue normal.

---

## Gaps funcionais (fazer)

### G1. Filtro de mês/ano nas listas

**Arquivos:** `ExpenseListScreen.kt` + `ExpenseViewModel.kt`, `IncomeListScreen.kt` + `IncomeViewModel.kt`

**Problema:** Lista mostra TODOS os gastos/entradas sem filtro de mês. Spec seção 11.3 e 11.2 exigem "Filtrar por mes".

**Correção:**
- Adicionar `selectedYear: Int` e `selectedMonth: Int` ao ViewModel
- Mudar `expenses` para `expenses` derivado de `repository.observeMonth(year, month)` (já existe no Repository)
- Botões `<` / `>` no topo da lista, similar ao que o dashboard já tem (`previousMonth` / `nextMonth`)
- Default = mês atual

**Critério de aceitação:** Criar gasto em maio e outro em junho. Na lista de Gastos, navegar entre maio e junho e ver o gasto certo em cada mês.

### G2. Tela de Recorrências

**Arquivos:**
- Criar `app/src/main/java/com/vitor/controlefinanceiro/ui/screens/recurring/RecurringListScreen.kt`
- Criar `app/src/main/java/com/vitor/controlefinanceiro/ui/screens/recurring/RecurringViewModel.kt`
- Atualizar `Routes.kt`, `AppNavHost.kt`, `MoreScreen.kt`, `FinanceApplication.kt`

**Problema:** Spec seção 19 define "Recorrencias" como item do menu Mais. Hoje só tem "Categorias" e "Backup" no Mais, e a frase "Recorrencias sao criadas ao marcar um gasto como recorrente." é um aviso estático. Usuário não consegue ver/editar/desativar recorrências criadas.

**Correção:**
- Criar `RecurringListScreen` com lista de recorrências (nome, valor, categoria, forma, dia de lançamento, status ativo/inativo)
- Cada item: botão "Desativar/Ativar" e botão "Excluir"
- Botão "+" abre form para criar recorrência avulsa (campos: nome, valor, categoria, forma, cartão condicional, dia lançamento 1-31, data início default=hoje, data fim opcional)
- Adicionar rota `Routes.Recurring = "recurring"`
- Adicionar `composable(Routes.Recurring) { RecurringListScreen() }` no `AppNavHost`
- Adicionar botão "Recorrências" no `MoreScreen` que navega para a rota
- ViewModel: injetar `RecurringExpenseRepository` via Koin

**Critério de aceitação:** Criar recorrência avulsa "Aluguel 1500 dia 5". Reabrir app. Conferir que `GenerateRecurringExpensesUseCase` gera despesa todo mês 5.

### G3. Data do último backup/importação na tela de Backup

**Arquivos:** `BackupScreen.kt` + `BackupViewModel.kt`

**Problema:** Spec seção 11.8 exige mostrar "Ver data do ultimo backup exportado" e "Ver data da ultima importacao". Hoje não exibe nada. O `BackupRepository` salva essas datas em `app_metadata` mas a UI não lê.

**Correção:**
- ViewModel expõe `lastExportAt: StateFlow<Long?>` e `lastImportAt: StateFlow<Long?>`, lendo de `AppPreferencesRepository` (que já existe e já tem `setLastExportAt` / `setLastImportAt`)
- No `BackupRepository.exportJson`, além de salvar em metadata, chamar `appPreferencesRepository.setLastExportAt(dto.exportedAt)`
- No `BackupRepository.importReplacing`, chamar `appPreferencesRepository.setLastImportAt(DateUtils.nowMillis())`
- Na `BackupScreen`, exibir:
  ```
  Ultimo backup exportado: 15/06/2026 as 14:32
  Ultima importacao: 14/06/2026 as 09:10
  ```
  ou "Nunca" se for null.
- Injetar `AppPreferencesRepository` no `BackupViewModel` via Koin

**Critério de aceitação:** Exportar JSON. Reabrir tela de Backup. Conferir que aparece a data atual.

### G4. Seletor de mês/ano na fatura do cartão

**Arquivos:** `CreditCardListScreen.kt` + `CreditCardViewModel.kt`

**Problema:** O botão "Ver fatura do mes" do cartão usa `selected.second/third` que é o mês atual fixo. Usuário não consegue ver fatura de meses passados.

**Correção:**
- Na seção "Fatura X/Y" da lista, adicionar botões `<` / `>` ao lado do título para navegar mês a mês
- ViewModel: novo método `changeInvoiceMonth(delta: Int)` que ajusta `selectedInvoice` mantendo o `cardId`
- Default = mês atual ao clicar em "Ver fatura do mes"

**Critério de aceitação:** Cartão cadastrado, criar 1 gasto no cartão em maio e outro em junho. Ver fatura de maio: só aparece o gasto de maio. Ver fatura de junho: só aparece o de junho.

---

## Polimento (fazer se sobrar tempo)

### P1. Mais testes unitários

Adicionar testes em `app/src/test/java/com/vitor/controlefinanceiro/`:
- `DateUtilsTest.kt`: `parseBrDateOrNull` casos válidos e inválidos
- `RecurringIdempotencyTest`: garante que criar recorrência + rodar `GenerateRecurringExpensesUseCase` duas vezes gera só 1 despesa por mês
- `BackupImportRoundTripTest`: exporta um DTO com dados, importa de volta, confere que tudo bate

Não precisa testar UI (sem Compose UI tests nessa spec).

---

## Mudanças esperadas (resumo de arquivos)

### Criar
- `app/src/main/java/com/vitor/controlefinanceiro/ui/screens/recurring/RecurringListScreen.kt`
- `app/src/main/java/com/vitor/controlefinanceiro/ui/screens/recurring/RecurringViewModel.kt`
- `app/src/test/java/com/vitor/controlefinanceiro/DateUtilsTest.kt`
- `app/src/test/java/com/vitor/controlefinanceiro/RecurringIdempotencyTest.kt`
- `app/src/test/java/com/vitor/controlefinanceiro/BackupImportRoundTripTest.kt`

### Editar
- `app/src/main/java/com/vitor/controlefinanceiro/ui/screens/expenses/ExpenseListScreen.kt`
- `app/src/main/java/com/vitor/controlefinanceiro/ui/screens/expenses/ExpenseViewModel.kt`
- `app/src/main/java/com/vitor/controlefinanceiro/ui/screens/incomes/IncomeListScreen.kt`
- `app/src/main/java/com/vitor/controlefinanceiro/ui/screens/incomes/IncomeViewModel.kt`
- `app/src/main/java/com/vitor/controlefinanceiro/ui/screens/cards/CreditCardListScreen.kt`
- `app/src/main/java/com/vitor/controlefinanceiro/ui/screens/cards/CreditCardViewModel.kt`
- `app/src/main/java/com/vitor/controlefinanceiro/ui/screens/categories/CategoryListScreen.kt`
- `app/src/main/java/com/vitor/controlefinanceiro/ui/screens/categories/CategoryViewModel.kt`
- `app/src/main/java/com/vitor/controlefinanceiro/ui/screens/dashboard/DashboardScreen.kt`
- `app/src/main/java/com/vitor/controlefinanceiro/ui/screens/dashboard/DashboardViewModel.kt`
- `app/src/main/java/com/vitor/controlefinanceiro/ui/screens/backup/BackupScreen.kt`
- `app/src/main/java/com/vitor/controlefinanceiro/ui/screens/backup/BackupViewModel.kt`
- `app/src/main/java/com/vitor/controlefinanceiro/ui/screens/more/MoreScreen.kt`
- `app/src/main/java/com/vitor/controlefinanceiro/ui/navigation/AppNavHost.kt`
- `app/src/main/java/com/vitor/controlefinanceiro/ui/navigation/Routes.kt`
- `app/src/main/java/com/vitor/controlefinanceiro/data/backup/BackupRepository.kt`
- `app/src/main/java/com/vitor/controlefinanceiro/FinanceApplication.kt`
- `app/src/main/java/com/vitor/controlefinanceiro/core/date/DateUtils.kt`

**Total estimado: ~17 arquivos modificados, 5 criados.**

---

## Critérios de aceitação (build verde)

Antes de commitar, OpenCode deve garantir:

```bash
cd /home/vitor/Documentos/wsgit/financehome/financeHome
./gradlew clean
./gradlew compileDebugKotlin
./gradlew test
./gradlew assembleDebug
```

Todos devem sair com sucesso. APK gerado em:
```
app/build/outputs/apk/debug/app-debug.apk
```

OpenCode NÃO deve fazer push. Apenas commit local.

Whatson (eu) vai rodar `./gradlew assembleDebug` novamente ao final pra confirmar que o APK é buildável do zero e enviar ao Vitor via Telegram.

---

## Restrições

- **NÃO** adicionar dependências novas. Stack travada: Compose BOM 2024.12.01, Room 2.6.1, Koin 3.5.6, Navigation 2.8.5.
- **NÃO** mudar `compileSdk` (35) nem `minSdk` (26).
- **NÃO** criar entidades Room novas. As 6 atuais cobrem tudo.
- **NÃO** mexer em `BackupDto` (mantém compatibilidade com backups existentes, `schemaVersion: 1`).
- **NÃO** introduzir FlowRow em telas onde não tem hoje (já tem em `ChipSelector`, ok).
- **NÃO** traduzir termos técnicos: `feature`, `bottom nav`, `FAB` podem ficar em inglês nos comentários, mas textos de UI em pt-BR.

---

## Ordem sugerida de implementação

1. B1 (bug recorrência) + helper `parseBrDateOrNull` em DateUtils — base pra B2
2. B2 (form de despesa com datas)
3. B3 (edição de entidades) — reaproveita o form de B2
4. B4 (dashboard limpo + últimos lançamentos)
5. G1 (filtro mês/ano nas listas)
6. G2 (tela de recorrências)
7. G3 (datas de backup)
8. G4 (fatura do cartão navegável)
9. P1 (testes extras) — opcional
10. Build verde + commit

---

## Resumo executivo

| Categoria | Itens | Esforço |
|-----------|-------|---------|
| Bugs críticos | 4 (B1-B4) | Médio |
| Gaps funcionais | 4 (G1-G4) | Médio |
| Polimento | 1 (P1) | Baixo (opcional) |
| Arquivos | ~17 editados, 5 criados | |
| Risco | Baixo — sem mudança de stack, sem migração de schema Room (B1 é lógica, não coluna) | |
