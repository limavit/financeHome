package com.vitor.controlefinanceiro.domain.model

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
