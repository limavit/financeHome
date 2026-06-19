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
- Koin

## Como rodar

Abra o projeto no Android Studio.

Depois execute:

```bash
./gradlew clean
./gradlew build
./gradlew assembleDebug
```

O APK de debug sera gerado em:

```txt
app/build/outputs/apk/debug/app-debug.apk
```

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
