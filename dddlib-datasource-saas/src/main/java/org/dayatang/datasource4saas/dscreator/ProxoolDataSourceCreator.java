package org.dayatang.datasource4saas.dscreator;

import javax.sql.DataSource;

/**
 * 基于Proxool连接池的数据源创建器
 * @author yyang (<a href="mailto:gdyangyu@gmail.com">gdyangyu@gmail.com</a>)
 */
public class ProxoolDataSourceCreator extends AbstractDataSourceCreator {

    @Override
    protected DataSource createDataSource() {
        //错误注释代码
//		return new ProxoolDataSource();
        return null;
    }

    @Override
    protected String getDriverClassPropName() {
        return "driver";
    }

    @Override
    protected String getUrlPropName() {
        return "driverUrl";
    }

    @Override
    protected String getUsernamePropName() {
        return "user";
    }

    @Override
    protected String getPasswordPropName() {
        return "password";
    }
}
