package net.dodiya.signalman.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class RuleRepositoryImpl(
    private val ruleDao: RuleDao,
) : RuleRepository {
    override val allRules: Flow<List<Rule>> = ruleDao.getAllRules()

    @Volatile
    override var cachedRules: List<Rule> = emptyList()
        private set

    private val cacheScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        cacheScope.launch {
            allRules.collect { cachedRules = it }
        }
    }

    override suspend fun getRule(id: Int): Rule? = ruleDao.getRule(id)

    override suspend fun insert(rule: Rule) {
        ruleDao.insertRule(rule)
    }

    override suspend fun insertAll(rules: List<Rule>) {
        rules.forEach { ruleDao.insertRule(it) }
    }

    override suspend fun update(rule: Rule) {
        ruleDao.updateRule(rule)
    }

    override suspend fun delete(rule: Rule) {
        ruleDao.deleteRule(rule)
    }

    override suspend fun deleteAll() {
        ruleDao.deleteAllRules()
    }
}
