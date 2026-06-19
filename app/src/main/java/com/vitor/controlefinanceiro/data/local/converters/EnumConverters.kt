package com.vitor.controlefinanceiro.data.local.converters

import androidx.room.TypeConverter
import com.vitor.controlefinanceiro.domain.model.CategoryType
import com.vitor.controlefinanceiro.domain.model.ExpenseStatus
import com.vitor.controlefinanceiro.domain.model.IncomeType
import com.vitor.controlefinanceiro.domain.model.PaymentMethod

class EnumConverters {
    @TypeConverter fun incomeTypeToString(value: IncomeType): String = value.name
    @TypeConverter fun stringToIncomeType(value: String): IncomeType = IncomeType.valueOf(value)
    @TypeConverter fun paymentMethodToString(value: PaymentMethod): String = value.name
    @TypeConverter fun stringToPaymentMethod(value: String): PaymentMethod = PaymentMethod.valueOf(value)
    @TypeConverter fun expenseStatusToString(value: ExpenseStatus): String = value.name
    @TypeConverter fun stringToExpenseStatus(value: String): ExpenseStatus = ExpenseStatus.valueOf(value)
    @TypeConverter fun categoryTypeToString(value: CategoryType): String = value.name
    @TypeConverter fun stringToCategoryType(value: String): CategoryType = CategoryType.valueOf(value)
}
