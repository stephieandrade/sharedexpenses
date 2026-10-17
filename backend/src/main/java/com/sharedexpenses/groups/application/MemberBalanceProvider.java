package com.sharedexpenses.groups.application;

import java.math.BigDecimal;

public interface MemberBalanceProvider {

    BigDecimal balanceOf(Long groupId, Long userId);
}
