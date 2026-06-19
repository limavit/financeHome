# Contexto do Projeto - App Android Controle Financeiro Local

## 1. Resumo do projeto

Criar um aplicativo Android nativo chamado **Controle Financeiro Local**.

O app sera usado localmente no celular, sem login, sem backend e sem publicacao em loja de aplicativos. O objetivo e gerar um APK instalavel manualmente em Android.

O app deve funcionar totalmente offline e armazenar os dados em banco local. O usuario deve conseguir exportar todos os dados em um arquivo JSON e importar esse mesmo JSON em outro celular usando o mesmo app.

## 2. Decisao tecnica principal

Stack recomendada e obrigatoria para o projeto:

```txt
Linguagem: Kotlin
UI: Jetpack Compose + Material 3
Arquitetura: MVVM com Repository
Banco local: Room Database usando SQLite
Preferencias/configuracoes: DataStore Preferences
Serializacao JSON: kotlinx.serialization
Navegacao: Navigation Compose
Injecao de dependencia: Hilt ou Koin
Build: Gradle Kotlin DSL
Min SDK: 26 ou superior
Target SDK: versao estavel mais recente disponivel no ambiente
Distribuicao: APK instalavel manualmente
```

Nao usar backend, Firebase, banco online, login, conta de usuario ou sincronizacao em nuvem nesta primeira versao.

## 3. Objetivo funcional

O aplicativo deve ser um controle financeiro pessoal simples, com:

- Cadastro de entradas.
- Cadastro de gastos.
- Categorias pre-cadastradas.
- Criacao de novas categorias pelo usuario.
- Separacao por forma de pagamento.
- Controle de dinheiro, Pix, debito, boleto e cartao de credito.
- Cadastro de multiplos cartoes de credito.
- Apelido para cada cartao cadastrado.
- Dia de fechamento da fatura de cada cartao.
- Dia de vencimento da fatura de cada cartao.
- Lancamentos recorrentes mensais automaticos.
- Exportacao completa dos dados em JSON.
- Importacao completa dos dados a partir de JSON.
- Backup manual para trocar de celular.

## 4. Regras de entradas

O usuario deve poder cadastrar entradas financeiras.

Tipos principais pre-cadastrados:

```txt
SALARIO
ADIANTAMENTO
RETIRADA_INVESTIMENTO
PRESTACAO_SERVICO
OUTROS
```

Campos de uma entrada:

```txt
id
descricao
valor
tipo
data
observacao
createdAt
updatedAt
```

Regras:

- Valor deve ser maior que zero.
- Data e obrigatoria.
- Descricao e obrigatoria.
- Tipo e obrigatorio.
- A entrada deve impactar positivamente o saldo do mes.

## 5. Regras de gastos

O usuario deve poder cadastrar gastos.

Campos de um gasto:

```txt
id
nome
descricao
valor
dataCompra
dataVencimento
dataPagamento
categoriaId
formaPagamento
status
cartaoCreditoId
recorrente
recorrenciaId
observacao
createdAt
updatedAt
```

Formas de pagamento:

```txt
DINHEIRO
PIX
DEBITO
BOLETO
CARTAO_CREDITO
```

Status:

```txt
ABERTO
PAGO
CANCELADO
```

Regras:

- Nome e obrigatorio.
- Valor deve ser maior que zero.
- Categoria e obrigatoria.
- Forma de pagamento e obrigatoria.
- Se a forma de pagamento for `CARTAO_CREDITO`, o campo `cartaoCreditoId` e obrigatorio.
- Se a forma de pagamento nao for cartao de credito, `cartaoCreditoId` deve ser nulo.
- Gasto pago em dinheiro, Pix, debito ou boleto deve impactar diretamente o mes da data do gasto ou vencimento.
- Gasto em cartao de credito deve entrar na fatura correta do cartao.

## 6. Cartoes de credito

O app deve permitir cadastrar mais de um cartao de credito.

Campos do cartao:

```txt
id
apelido
bancoOuInstituicao
limite
diaFechamentoFatura
diaVencimentoFatura
ativo
createdAt
updatedAt
```

Regras:

- O apelido e obrigatorio.
- Dia de fechamento deve aceitar valores de 1 a 31.
- Dia de vencimento deve aceitar valores de 1 a 31.
- O usuario pode cadastrar cartoes como Nubank, Inter, Itau, Mercado Pago, Cartao da empresa ou outros.
- O app nao deve consultar banco, API ou internet.
- O limite e opcional.
- Cartoes inativos nao devem aparecer como opcao padrao em novos gastos.
- Cartoes inativos devem continuar aparecendo em relatorios antigos.

## 7. Regra de fatura do cartao

Implementar uma funcao de dominio para calcular a fatura de um gasto no cartao.

Exemplo:

```txt
Cartao:
Fechamento: dia 10
Vencimento: dia 20

Compra feita em 05/06:
entra na fatura que fecha em 10/06 e vence em 20/06.

Compra feita em 11/06:
entra na fatura que fecha em 10/07 e vence em 20/07.
```

Regra geral:

```txt
Se dia da compra <= diaFechamentoFatura:
    mes da fatura = mes da compra
Senao:
    mes da fatura = proximo mes
```

A data de vencimento da fatura deve ser calculada com base no `diaVencimentoFatura`.

Cuidado com meses que nao possuem dia 29, 30 ou 31. Nesses casos, usar o ultimo dia valido do mes.

Exemplo:

```txt
Vencimento dia 31 em fevereiro:
usar 28 ou 29, conforme o ano.
```

## 8. Recorrencia

O app deve permitir gastos recorrentes mensais.

Campos da recorrencia:

```txt
id
nome
valor
categoriaId
formaPagamento
cartaoCreditoId
diaLancamento
dataInicio
dataFim
ativa
observacao
createdAt
updatedAt
```

Regras:

- Recorrencia pode ser criada para gastos fixos como aluguel, internet, energia, mensalidade, streaming etc.
- O sistema deve lancar automaticamente o gasto recorrente mes a mes.
- O lancamento automatico deve ser idempotente: nao pode duplicar o mesmo gasto recorrente no mesmo mes.
- Ao abrir o app, executar uma rotina que verifica recorrencias ativas e cria os gastos pendentes ate o mes atual.
- Se `dataFim` estiver preenchida, nao gerar gastos depois dessa data.
- Se a forma for cartao de credito, usar a regra de fatura do cartao.
- Permitir ativar/desativar recorrencia.

Criar uma chave logica para evitar duplicidade:

```txt
recorrenciaId + ano + mes
```

## 9. Categorias

O app deve vir com categorias pre-cadastradas.

Categorias iniciais:

```txt
Moradia
Alimentacao
Mercado
Padaria
Transporte
Combustivel
Saude
Farmacia
Educacao
Filhos
Lazer
Restaurante
Cartao de Credito
Contas Fixas
Energia
Agua
Internet
Telefone
Investimentos
Impostos
Servicos
Outros
```

Campos da categoria:

```txt
id
nome
tipo
ativa
createdAt
updatedAt
```

Tipo:

```txt
ENTRADA
GASTO
AMBOS
```

Regras:

- Usuario pode criar novas categorias.
- Usuario pode editar categorias criadas por ele.
- Categorias pre-cadastradas podem ser desativadas, mas nao excluidas fisicamente.
- Categorias ja usadas em lancamentos nao podem ser excluidas fisicamente.

## 10. Dashboard inicial

Tela inicial deve mostrar o resumo do mes atual.

Cards obrigatorios:

```txt
Entradas do mes
Gastos do mes
Saldo do mes
Gastos em aberto
Gastos pagos
Total em cartao de credito
Total em dinheiro/Pix/debito/boleto
```

Tambem mostrar:

```txt
Lista dos ultimos lancamentos
Botao para adicionar entrada
Botao para adicionar gasto
Botao para acessar cartoes
Botao para acessar categorias
Botao para exportar/importar backup
```

Filtros:

```txt
Mes
Ano
Categoria
Forma de pagamento
Status
Cartao de credito
```

## 11. Telas necessarias

### 11.1 Tela Home / Dashboard

Deve conter:

- Resumo financeiro do mes.
- Filtro por mes/ano.
- Ultimos lancamentos.
- Atalhos principais.

### 11.2 Tela de entradas

Funcionalidades:

- Listar entradas.
- Criar entrada.
- Editar entrada.
- Excluir entrada.
- Filtrar por mes.
- Filtrar por tipo.

### 11.3 Tela de gastos

Funcionalidades:

- Listar gastos.
- Criar gasto.
- Editar gasto.
- Marcar como pago.
- Cancelar gasto.
- Excluir gasto.
- Filtrar por mes.
- Filtrar por categoria.
- Filtrar por forma de pagamento.
- Filtrar por status.
- Filtrar por cartao.

### 11.4 Tela de cadastro de gasto

Campos:

```txt
Nome
Descricao
Valor
Data da compra
Data de vencimento
Categoria
Forma de pagamento
Cartao de credito, quando necessario
Status
Recorrente: sim/nao
Observacao
```

Comportamento:

- Ao selecionar `CARTAO_CREDITO`, exibir campo de cartao.
- Ao selecionar outra forma, esconder campo de cartao.
- Se marcar recorrente, permitir configurar:
  - dia de lancamento
  - data inicial
  - data final opcional
  - ativa/inativa

### 11.5 Tela de cartoes

Funcionalidades:

- Listar cartoes.
- Criar cartao.
- Editar cartao.
- Desativar cartao.
- Ver gastos por cartao.
- Ver faturas calculadas por mes.

### 11.6 Tela de fatura do cartao

Para cada cartao, permitir visualizar:

```txt
Cartao
Mes da fatura
Data de fechamento
Data de vencimento
Total da fatura
Gastos da fatura
Status dos gastos
```

Nao precisa criar entidade `Fatura` obrigatoriamente. Pode calcular dinamicamente com base nos gastos do cartao.

### 11.7 Tela de categorias

Funcionalidades:

- Listar categorias.
- Criar categoria.
- Editar categoria.
- Desativar categoria.
- Exibir se e categoria de entrada, gasto ou ambos.

### 11.8 Tela de backup

Funcionalidades:

```txt
Exportar JSON
Importar JSON
Ver data do ultimo backup exportado
Ver data da ultima importacao
```

Ao exportar:

- Abrir seletor do Android para o usuario escolher onde salvar.
- Gerar arquivo `.json`.
- Nome sugerido:

```txt
controle-financeiro-backup-YYYY-MM-DD-HH-mm.json
```

Ao importar:

- Abrir seletor do Android para escolher arquivo `.json`.
- Validar estrutura.
- Mostrar confirmacao antes de substituir/mesclar dados.

Oferecer duas opcoes de importacao:

```txt
Substituir banco atual
Mesclar com dados existentes
```

Na primeira versao, implementar obrigatoriamente:

```txt
Substituir banco atual
```

A opcao de mesclar pode ficar preparada, mas nao precisa ser completa se aumentar muito a complexidade.

## 12. Modelo de banco de dados Room

Criar entidades:

```txt
CategoryEntity
IncomeEntity
ExpenseEntity
CreditCardEntity
RecurringExpenseEntity
AppMetadataEntity
```

### 12.1 CategoryEntity

```kotlin
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: CategoryType,
    val isSystem: Boolean,
    val active: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
```

### 12.2 IncomeEntity

```kotlin
@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey val id: String,
    val description: String,
    val amountCents: Long,
    val type: IncomeType,
    val date: Long,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long
)
```

Usar `amountCents` em vez de `Double` para dinheiro.

Exemplo:

```txt
R$ 10,50 = 1050
R$ 100,00 = 10000
```

### 12.3 ExpenseEntity

```kotlin
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = CreditCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["creditCardId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("categoryId"),
        Index("creditCardId"),
        Index("recurringExpenseId"),
        Index("invoiceYear"),
        Index("invoiceMonth")
    ]
)
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val amountCents: Long,
    val purchaseDate: Long,
    val dueDate: Long?,
    val paymentDate: Long?,
    val categoryId: String,
    val paymentMethod: PaymentMethod,
    val status: ExpenseStatus,
    val creditCardId: String?,
    val recurring: Boolean,
    val recurringExpenseId: String?,
    val recurringYear: Int?,
    val recurringMonth: Int?,
    val invoiceYear: Int?,
    val invoiceMonth: Int?,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long
)
```

### 12.4 CreditCardEntity

```kotlin
@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @PrimaryKey val id: String,
    val nickname: String,
    val institution: String?,
    val limitCents: Long?,
    val closingDay: Int,
    val dueDay: Int,
    val active: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
```

### 12.5 RecurringExpenseEntity

```kotlin
@Entity(
    tableName = "recurring_expenses",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = CreditCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["creditCardId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("categoryId"),
        Index("creditCardId")
    ]
)
data class RecurringExpenseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val amountCents: Long,
    val categoryId: String,
    val paymentMethod: PaymentMethod,
    val creditCardId: String?,
    val launchDay: Int,
    val startDate: Long,
    val endDate: Long?,
    val active: Boolean,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long
)
```

### 12.6 AppMetadataEntity

```kotlin
@Entity(tableName = "app_metadata")
data class AppMetadataEntity(
    @PrimaryKey val key: String,
    val value: String
)
```

## 13. Enums

Criar enums:

```kotlin
enum class IncomeType {
    SALARIO,
    ADIANTAMENTO,
    RETIRADA_INVESTIMENTO,
    PRESTACAO_SERVICO,
    OUTROS
}

enum class PaymentMethod {
    DINHEIRO,
    PIX,
    DEBITO,
    BOLETO,
    CARTAO_CREDITO
}

enum class ExpenseStatus {
    ABERTO,
    PAGO,
    CANCELADO
}

enum class CategoryType {
    ENTRADA,
    GASTO,
    AMBOS
}
```

## 14. Estrutura JSON de backup

O JSON exportado deve conter versao de schema.

Exemplo:

```json
{
  "schemaVersion": 1,
  "exportedAt": 1719000000000,
  "appName": "Controle Financeiro Local",
  "categories": [],
  "incomes": [],
  "expenses": [],
  "creditCards": [],
  "recurringExpenses": [],
  "metadata": {}
}
```

Criar DTOs separados das entidades Room:

```txt
BackupDto
CategoryBackupDto
IncomeBackupDto
ExpenseBackupDto
CreditCardBackupDto
RecurringExpenseBackupDto
```

Regras:

- Nunca exportar caminho interno do banco.
- Exportar apenas dados necessarios.
- Importacao deve validar:
  - `schemaVersion`
  - listas obrigatorias
  - campos obrigatorios
  - valores monetarios maiores ou iguais a zero
  - enums validos
- Se o JSON for invalido, exibir mensagem amigavel.
- A importacao com substituicao deve rodar dentro de uma transacao Room.
- Antes de importar substituindo, exibir confirmacao:

```txt
Atencao: a importacao vai substituir todos os dados atuais deste aparelho. Deseja continuar?
```

## 15. Rotina de exportacao/importacao

Usar Storage Access Framework.

### Exportar

Fluxo:

```txt
Usuario toca em Exportar
App gera JSON em memoria
App abre seletor ACTION_CREATE_DOCUMENT
Usuario escolhe local
App grava JSON no Uri escolhido
App mostra sucesso
```

### Importar

Fluxo:

```txt
Usuario toca em Importar
App abre seletor ACTION_OPEN_DOCUMENT
Usuario escolhe arquivo JSON
App le conteudo
App valida estrutura
App mostra tela de confirmacao
App substitui dados dentro de transacao
App mostra sucesso
```

## 16. Arquitetura de pastas

Criar estrutura:

```txt
app/
  src/main/java/com/vitor/controlefinanceiro/
    MainActivity.kt

    core/
      money/
        MoneyFormatter.kt
      date/
        DateUtils.kt
      json/
        JsonConfig.kt
      result/
        AppResult.kt

    data/
      local/
        AppDatabase.kt
        converters/
          EnumConverters.kt
        dao/
          CategoryDao.kt
          IncomeDao.kt
          ExpenseDao.kt
          CreditCardDao.kt
          RecurringExpenseDao.kt
          MetadataDao.kt
        entity/
          CategoryEntity.kt
          IncomeEntity.kt
          ExpenseEntity.kt
          CreditCardEntity.kt
          RecurringExpenseEntity.kt
          AppMetadataEntity.kt

      backup/
        BackupDto.kt
        BackupRepository.kt
        BackupValidator.kt

      repository/
        CategoryRepository.kt
        IncomeRepository.kt
        ExpenseRepository.kt
        CreditCardRepository.kt
        RecurringExpenseRepository.kt
        DashboardRepository.kt

    domain/
      model/
        CategoryType.kt
        IncomeType.kt
        PaymentMethod.kt
        ExpenseStatus.kt
      usecase/
        CalculateCreditCardInvoiceUseCase.kt
        GenerateRecurringExpensesUseCase.kt
        ExportBackupUseCase.kt
        ImportBackupUseCase.kt
        GetDashboardSummaryUseCase.kt

    ui/
      navigation/
        AppNavHost.kt
        Routes.kt

      theme/
        Color.kt
        Theme.kt
        Type.kt

      screens/
        dashboard/
          DashboardScreen.kt
          DashboardViewModel.kt
          DashboardUiState.kt

        incomes/
          IncomeListScreen.kt
          IncomeFormScreen.kt
          IncomeViewModel.kt

        expenses/
          ExpenseListScreen.kt
          ExpenseFormScreen.kt
          ExpenseViewModel.kt

        cards/
          CreditCardListScreen.kt
          CreditCardFormScreen.kt
          CreditCardInvoiceScreen.kt
          CreditCardViewModel.kt

        categories/
          CategoryListScreen.kt
          CategoryFormScreen.kt
          CategoryViewModel.kt

        backup/
          BackupScreen.kt
          BackupViewModel.kt
```

## 17. Regras de dashboard

Resumo mensal deve considerar:

```txt
Entradas:
somar IncomeEntity por mes/ano da data.

Gastos:
somar ExpenseEntity por mes/ano conforme regra:

- DINHEIRO, PIX, DEBITO, BOLETO:
  considerar purchaseDate ou dueDate, conforme existir.
- CARTAO_CREDITO:
  considerar invoiceYear e invoiceMonth calculados.
```

Saldo:

```txt
saldo = entradas - gastos
```

Separar:

```txt
totalEntradas
totalGastos
saldo
totalPago
totalAberto
totalCartaoCredito
totalDinheiro
totalPix
totalDebito
totalBoleto
```

## 18. Regras de dinheiro

Nunca usar `Double` para dinheiro.

Usar `Long amountCents`.

Criar formatter:

```kotlin
fun formatCentsToBrl(amountCents: Long): String
```

Exemplo:

```txt
1050 -> R$ 10,50
100000 -> R$ 1.000,00
```

Na UI, aceitar entrada como texto:

```txt
10,50
1000
1.000,00
```

Converter para centavos de forma segura.

## 19. UX minima

O app deve ser simples, direto e funcional.

Usar Material 3.

Navegacao inferior:

```txt
Inicio
Gastos
Entradas
Cartoes
Mais
```

Menu Mais:

```txt
Categorias
Recorrencias
Backup
Configuracoes
```

Usar FloatingActionButton nas telas principais para adicionar novo item.

Mensagens de erro devem ser claras:

```txt
Informe um valor valido.
Selecione uma categoria.
Selecione um cartao de credito.
O dia de fechamento deve estar entre 1 e 31.
Backup importado com sucesso.
Arquivo de backup invalido.
```

## 20. Primeira execucao

Na primeira abertura do app:

1. Criar categorias padrao.
2. Criar metadado `first_run_completed = true`.
3. Executar rotina de recorrencias.
4. Abrir dashboard.

## 21. Testes obrigatorios

Criar testes unitarios para:

```txt
Conversao de dinheiro para centavos.
Formatacao de centavos para BRL.
Calculo de fatura de cartao.
Geracao de recorrencias mensais.
Prevencao de duplicidade de recorrencias.
Validacao de backup JSON.
Importacao com substituicao.
```

Casos de teste de fatura:

```txt
Fechamento 10, vencimento 20, compra dia 05:
fatura do mesmo mes.

Fechamento 10, vencimento 20, compra dia 11:
fatura do proximo mes.

Vencimento dia 31 em fevereiro:
usar ultimo dia do mes.

Compra em dezembro apos fechamento:
fatura em janeiro do ano seguinte.
```

## 22. Criterios de aceite

O app so estara pronto quando:

```txt
1. Compilar sem erro.
2. Abrir no emulador Android.
3. Criar entrada.
4. Criar gasto em dinheiro.
5. Criar gasto em Pix.
6. Criar gasto em boleto.
7. Criar gasto em debito.
8. Criar cartao de credito.
9. Criar gasto em cartao.
10. Calcular fatura corretamente.
11. Criar gasto recorrente.
12. Gerar gastos recorrentes automaticamente ao abrir o app.
13. Nao duplicar recorrencias no mesmo mes.
14. Criar categoria personalizada.
15. Exportar JSON.
16. Importar JSON substituindo banco local.
17. Gerar APK instalavel.
```

## 23. Comandos esperados

O projeto deve permitir executar:

```bash
./gradlew clean
./gradlew build
./gradlew test
./gradlew assembleDebug
./gradlew assembleRelease
```

APK debug esperado:

```txt
app/build/outputs/apk/debug/app-debug.apk
```

APK release esperado:

```txt
app/build/outputs/apk/release/app-release.apk
```

## 24. README que deve ser criado pelo Codex

Criar tambem um arquivo `README.md` no projeto com o seguinte conteudo base:

```md
# Controle Financeiro Local

Aplicativo Android offline para controle financeiro pessoal.

## Funcionalidades

- Cadastro de entradas
- Cadastro de gastos
- Categorias personalizadas
- Controle por forma de pagamento
- Cadastro de multiplos cartoes de credito
- Fechamento e vencimento de faturas
- Gastos recorrentes mensais
- Exportacao de backup em JSON
- Importacao de backup em JSON
- Uso totalmente local/offline

## Stack

- Kotlin
- Jetpack Compose
- Material 3
- Room Database
- SQLite
- DataStore
- kotlinx.serialization
- Navigation Compose

## Como rodar

Abra o projeto no Android Studio.

Depois execute:

./gradlew clean
./gradlew build
./gradlew assembleDebug

O APK de debug sera gerado em:

app/build/outputs/apk/debug/app-debug.apk

## Como instalar no celular

1. Gere o APK.
2. Copie o APK para o celular.
3. Abra o arquivo no Android.
4. Autorize instalacao de fonte desconhecida, se necessario.
5. Instale o aplicativo.

## Backup

O app permite exportar todos os dados em JSON.

O arquivo pode ser salvo no celular, Google Drive, pendrive ou enviado para outro aparelho.

Para restaurar em outro celular:

1. Instale o app.
2. Abra a tela Backup.
3. Toque em Importar JSON.
4. Selecione o arquivo exportado.
5. Confirme a substituicao dos dados locais.

## Observacao

O app nao envia dados para servidor.
Todos os dados ficam armazenados localmente no aparelho.
```

## 25. Plano de implementacao em fases

Para evitar que o agente se perca, implementar em fases.

### Fase 1 - Base do projeto

```txt
Criar projeto Android Kotlin com Compose, Room, Navigation e estrutura MVVM.
Criar entidades, DAOs, database, converters e categorias iniciais.
Garantir que o app abre no Dashboard.
```

### Fase 2 - Entradas e categorias

```txt
Implementar cadastro, listagem, edicao e exclusao de entradas.
Implementar categorias pre-cadastradas e criacao de novas categorias.
```

### Fase 3 - Gastos

```txt
Implementar cadastro, listagem, edicao, exclusao, status e filtros de gastos.
Implementar formas de pagamento.
```

### Fase 4 - Cartoes

```txt
Implementar cadastro de cartoes.
Implementar calculo de fatura.
Implementar tela de fatura por cartao e mes.
```

### Fase 5 - Recorrencias

```txt
Implementar gastos recorrentes mensais.
Executar geracao automatica ao abrir o app.
Garantir que nao duplica lancamentos.
```

### Fase 6 - Backup

```txt
Implementar exportacao JSON.
Implementar importacao JSON.
Validar schema.
Importar substituindo banco atual dentro de transacao.
```

### Fase 7 - Polimento e APK

```txt
Ajustar UI.
Criar README.
Criar testes.
Gerar APK debug e release.
```

## 26. Instrucao final para o Codex

Implemente o projeto completo, priorizando funcionamento real acima de aparencia.

Nao deixe telas falsas ou botoes sem acao.

Quando necessario, simplifique a UI, mas mantenha as regras de negocio funcionando.

Ao terminar, entregue:

```txt
1. Codigo completo.
2. Instrucoes para rodar no Android Studio.
3. Instrucoes para gerar APK.
4. Lista do que foi implementado.
5. Lista de limitacoes conhecidas.
```

## 27. Decisao final de arquitetura

Usar:

```txt
Room/SQLite como fonte principal dos dados.
JSON como formato de backup manual.
Storage Access Framework para salvar/importar arquivo.
APK manual para instalacao fora da Play Store.
```

Essa arquitetura atende ao objetivo do projeto: app pessoal, local, simples, exportavel e reinstalavel em outro celular.
