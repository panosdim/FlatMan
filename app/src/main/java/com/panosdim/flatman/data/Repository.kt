package com.panosdim.flatman.data

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.panosdim.flatman.TAG
import com.panosdim.flatman.models.Flat
import com.panosdim.flatman.models.Response
import com.panosdim.flatman.models.Transaction
import com.panosdim.flatman.utils.TransactionType
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import java.math.BigDecimal

class Repository {
    private val user = Firebase.auth.currentUser
    private val database = Firebase.database
    private var listeners: MutableMap<DatabaseReference, ValueEventListener> = mutableMapOf()

    fun getFlats(): Flow<Response<List<Flat>>> = callbackFlow {
        val dbRef = user?.let { database.getReference(it.uid).child("flats") }

        val listener = dbRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(Response.Loading)
                val items = mutableListOf<Flat>()
                snapshot.children.forEach { flat ->
                    val itm = flat.getValue(Flat::class.java)
                    if (itm != null) {
                        itm.id = flat.key.toString()
                        items.add(itm)
                    }
                }

                trySend(Response.Success(items))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Response.Error(error.message))
                cancel()
            }

        })

        listener?.let {
            listeners.put(dbRef, it)
        }
        awaitClose {
            listener?.let {
                dbRef.removeEventListener(it)
                listeners.remove(dbRef, it)
            }
        }

        Log.d(TAG, "getFlats")
    }

    fun getSavings(): Flow<BigDecimal> {
        Log.d(TAG, "getSavings")
        val totalExpenses = getTotalExpenses()
        val totalRents = getTotalRents()
        return totalRents.combine(totalExpenses) { totRents, totExpenses -> totRents - totExpenses }
    }

    private fun getTotalExpenses(): Flow<BigDecimal> = callbackFlow {
        val dbRef = user?.let { database.getReference(it.uid).child("expenses") }

        var totalExpenses: BigDecimal

        val listener = dbRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val expenses =
                    snapshot.getValue<MutableMap<String, MutableMap<String, Transaction>>>()

                if (expenses != null) {
                    totalExpenses = BigDecimal.ZERO
                    expenses.values.forEach { exp ->
                        totalExpenses += exp.values.sumOf { it.amount.toBigDecimal() }
                    }
                    trySend(totalExpenses)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.toString())
                cancel()
            }

        })

        listener?.let {
            listeners.put(dbRef, it)
        }
        awaitClose {
            listener?.let {
                dbRef.removeEventListener(it)
                listeners.remove(dbRef, it)
            }
        }
    }

    private fun getTotalRents(): Flow<BigDecimal> = callbackFlow {
        val dbRef = user?.let { database.getReference(it.uid).child("rents") }

        var totalRents: BigDecimal

        val listener = dbRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val expenses =
                    snapshot.getValue<MutableMap<String, MutableMap<String, Transaction>>>()

                if (expenses != null) {
                    totalRents = BigDecimal.ZERO
                    expenses.values.forEach { exp ->
                        totalRents += exp.values.sumOf { it.amount.toBigDecimal() }
                    }
                    trySend(totalRents)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.toString())
                cancel()
            }

        })

        listener?.let {
            listeners.put(dbRef, it)
        }
        awaitClose {
            listener?.let {
                dbRef.removeEventListener(it)
                listeners.remove(dbRef, it)
            }
        }
    }

    fun getSavings(flatId: String): Flow<BigDecimal> {
        Log.d(TAG, "getSavings for specific flat $flatId")
        val totalExpenses = getTotalExpenses(flatId)
        val totalRents = getTotalRents(flatId)
        return totalRents.combine(totalExpenses) { totRents, totExpenses -> totRents - totExpenses }
    }

    private fun getTotalExpenses(flatId: String): Flow<BigDecimal> = callbackFlow {
        val dbRef = user?.let { database.getReference(it.uid).child("expenses").child(flatId) }

        var totalExpenses: BigDecimal

        val listener = dbRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val expenses =
                    snapshot.getValue<MutableMap<String, Transaction>>()

                if (expenses != null) {
                    totalExpenses = expenses.values.sumOf { it.amount.toBigDecimal() }
                    trySend(totalExpenses)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.toString())
                cancel()
            }

        })

        listener?.let {
            listeners.put(dbRef, it)
        }
        awaitClose {
            listener?.let {
                dbRef.removeEventListener(it)
                listeners.remove(dbRef, it)
            }
        }
    }

    private fun getTotalRents(flatId: String): Flow<BigDecimal> = callbackFlow {
        val dbRef = user?.let { database.getReference(it.uid).child("rents").child(flatId) }

        var totalRents: BigDecimal

        val listener = dbRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rents =
                    snapshot.getValue<MutableMap<String, Transaction>>()

                if (rents != null) {
                    totalRents = rents.values.sumOf { it.amount.toBigDecimal() }
                    trySend(totalRents)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.toString())
                cancel()
            }

        })

        listener?.let {
            listeners.put(dbRef, it)
        }
        awaitClose {
            listener?.let {
                dbRef.removeEventListener(it)
                listeners.remove(dbRef, it)
            }
        }
    }

    fun getExpenses(flatId: String): Flow<List<Transaction>> = callbackFlow {
        val dbRef = user?.let { database.getReference(it.uid).child("expenses").child(flatId) }

        val listener = dbRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val expenses = mutableListOf<Transaction>()
                snapshot.children.forEach { expense ->
                    val itm = expense.getValue(Transaction::class.java)
                    if (itm != null) {
                        itm.id = expense.key.toString()
                        expenses.add(itm)
                    }
                }

                expenses.sortByDescending { it.date }

                trySend(expenses)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.toString())
                cancel()
            }

        })

        listener?.let {
            listeners.put(dbRef, it)
        }
        awaitClose {
            listener?.let {
                dbRef.removeEventListener(it)
                listeners.remove(dbRef, it)
            }
        }

        Log.d(TAG, "getExpenses")
    }

    fun getRents(flatId: String): Flow<List<Transaction>> = callbackFlow {
        val dbRef = user?.let { database.getReference(it.uid).child("rents").child(flatId) }

        val listener = dbRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rents = mutableListOf<Transaction>()
                snapshot.children.forEach { rent ->
                    val itm = rent.getValue(Transaction::class.java)
                    if (itm != null) {
                        itm.id = rent.key.toString()
                        rents.add(itm)
                    }
                }

                rents.sortByDescending { it.date }

                trySend(rents)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.toString())
                cancel()
            }

        })

        listener?.let {
            listeners.put(dbRef, it)
        }
        awaitClose {
            listener?.let {
                dbRef.removeEventListener(it)
                listeners.remove(dbRef, it)
            }
        }

        Log.d(TAG, "getRents")
    }

    fun getLastRent(flatId: String): Flow<Transaction> = callbackFlow {
        val dbRef = user?.let { database.getReference(it.uid).child("rents").child(flatId) }

        val listener = dbRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rents = mutableListOf<Transaction>()
                snapshot.children.forEach { rent ->
                    val itm = rent.getValue(Transaction::class.java)
                    if (itm != null) {
                        itm.id = rent.key.toString()
                        rents.add(itm)
                    }
                }

                rents.sortByDescending { it.date }

                trySend(rents[0])
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, error.toString())
                cancel()
            }

        })

        listener?.let {
            listeners.put(dbRef, it)
        }
        awaitClose {
            listener?.let {
                dbRef.removeEventListener(it)
                listeners.remove(dbRef, it)
            }
        }

        Log.d(TAG, "getLastRent")
    }

    fun addTransaction(
        flatId: String,
        transaction: Transaction,
        transactionType: TransactionType
    ): Flow<Boolean> =
        callbackFlow {
            val dbRef = user?.let {
                database.getReference(it.uid).child(transactionType.getFirebasePath()).child(flatId)
            }

            dbRef?.push()?.setValue(transaction)
                ?.addOnSuccessListener {
                    trySend(true)
                    cancel()
                }
                ?.addOnFailureListener {
                    trySend(false)
                    cancel()
                }

            awaitClose {
                channel.close()
            }
        }

    fun updateTransaction(
        flatId: String,
        transaction: Transaction,
        transactionType: TransactionType
    ): Flow<Boolean> =
        callbackFlow {
            val dbRef = user?.let {
                database.getReference(it.uid).child(transactionType.getFirebasePath()).child(flatId)
                    .child(transaction.id)
            }

            dbRef?.setValue(transaction)
                ?.addOnSuccessListener {
                    trySend(true)
                    cancel()
                }
                ?.addOnFailureListener {
                    trySend(false)
                    cancel()
                }
            dbRef?.child("id")?.removeValue()

            awaitClose {
                channel.close()
            }
        }

    fun deleteTransaction(
        flatId: String,
        transaction: Transaction,
        transactionType: TransactionType
    ): Flow<Boolean> =
        callbackFlow {
            val dbRef = user?.let {
                database.getReference(it.uid).child(transactionType.getFirebasePath()).child(flatId)
                    .child(transaction.id)
            }

            dbRef?.removeValue()
                ?.addOnSuccessListener {
                    trySend(true)
                    cancel()
                }
                ?.addOnFailureListener {
                    trySend(false)
                    cancel()
                }

            awaitClose {
                channel.close()
            }
        }

    fun addNewItem(item: Flat): Flow<Boolean> {
        return callbackFlow {
            val itemsRef = user?.let { database.getReference("items").child(it.uid) }

            itemsRef?.push()?.setValue(item)
                ?.addOnSuccessListener {
                    trySend(true)
                    cancel()
                }
                ?.addOnFailureListener {
                    trySend(false)
                    cancel()
                }

            awaitClose {
                channel.close()
            }
        }
    }

    fun updateItem(item: Flat): Flow<Boolean> {
        val itemsRef = user?.let {
            item.id.let { id ->
                database.getReference("items").child(it.uid).child(
                    id
                )
            }
        }

        return callbackFlow {
            itemsRef?.setValue(item)
                ?.addOnSuccessListener {
                    trySend(true)
                    cancel()
                }?.addOnFailureListener {
                    trySend(false)
                    cancel()
                }
            itemsRef?.child("id")?.removeValue()

            awaitClose {
                channel.close()
            }
        }
    }

    fun deleteItem(item: Flat): Flow<Boolean> {
        val itemsRef = user?.let {
            item.id.let { id ->
                database.getReference("items").child(it.uid).child(
                    id
                )
            }
        }

        return callbackFlow {
            itemsRef?.removeValue()
                ?.addOnSuccessListener {
                    trySend(true)
                    cancel()
                }?.addOnFailureListener {
                    trySend(false)
                    cancel()
                }

            awaitClose {
                channel.close()
            }
        }
    }

    fun signOut() {
        listeners.let { list ->
            list.forEach {
                it.key.removeEventListener(it.value)
            }
        }
    }
}