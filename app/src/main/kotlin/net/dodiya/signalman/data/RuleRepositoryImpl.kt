package net.dodiya.signalman.data

import kotlinx.coroutines.flow.Flow

class RuleRepositoryImpl(
    private val ruleDao: RuleDao,
) : RuleRepository {
    override val allRules: Flow<List<Rule>> = ruleDao.getAllRules()

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
