package com.sharedexpenses.users.application;

import com.sharedexpenses.users.domain.User;

public interface TokenIssuer {

    IssuedToken issue(User user);
}
