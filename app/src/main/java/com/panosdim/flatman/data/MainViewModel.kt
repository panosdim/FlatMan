package com.panosdim.flatman.data


import androidx.lifecycle.ViewModel
import com.panosdim.flatman.models.Flat
import com.panosdim.flatman.models.Transaction
import com.panosdim.flatman.utils.TransactionType
import kotlinx.coroutines.flow.Flow


class MainViewModel : ViewModel() {
    private val repository = Repository()
    val flats = repository.getFlats()

    fun addNewItem(item: Flat): Flow<Boolean> {
        return repository.addNewItem(item)
    }

    fun updateItem(item: Flat): Flow<Boolean> {
        return repository.updateItem(item)
    }

    fun deleteItem(item: Flat): Flow<Boolean> {
        return repository.deleteItem(item)
    }

    fun addTransaction(flatId: String, transaction: Transaction, transactionType: TransactionType) =
        repository.addTransaction(flatId, transaction, transactionType)

    fun updateTransaction(
        flatId: String,
        transaction: Transaction,
        transactionType: TransactionType
    ) =
        repository.updateTransaction(flatId, transaction, transactionType)

    fun deleteTransaction(
        flatId: String,
        transaction: Transaction,
        transactionType: TransactionType
    ) =
        repository.deleteTransaction(flatId, transaction, transactionType)

    fun getExpenses(flatId: String) = repository.getExpenses(flatId)

    fun getRents(flatId: String) = repository.getRents(flatId)

    fun getLastRent(flatId: String) = repository.getLastRent(flatId)

    fun getSavings() = repository.getSavings()

    fun getSavings(flatId: String) = repository.getSavings(flatId)

    fun signOut() {
        repository.signOut()
    }
}