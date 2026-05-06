package net.dodiya.signalman.data

import kotlinx.coroutines.flow.Flow

interface RuleRepository {
    val allRules: Flow<List<Rule>>
    val cachedRules: List<Rule>

    suspend fun getRule(id: Int): Rule?

    suspend fun insert(rule: Rule)

    suspend fun insertAll(rules: List<Rule>)

    suspend fun update(rule: Rule)

    suspend fun delete(rule: Rule)

    suspend fun deleteAll()
}
