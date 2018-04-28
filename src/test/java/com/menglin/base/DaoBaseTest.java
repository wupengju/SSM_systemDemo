package com.menglin.base;

import org.springframework.test.context.transaction.TransactionConfiguration;

// 默认回滚,即此类中的方法即使执行成功,数据也并不会真正的修改,方法执行后会回滚。【DAO 测试才需要，Service 都添加了相应的事务管理】
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class DaoBaseTest extends BaseTest {
}
