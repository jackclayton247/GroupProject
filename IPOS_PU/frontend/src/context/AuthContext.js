import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

const STORAGE_KEY = 'ipos_auth';

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      return stored ? JSON.parse(stored) : { isLoggedIn: false, userEmail: null, isMerchant: false, forcePasswordChange: false };
    } catch {
      return { isLoggedIn: false, userEmail: null, isMerchant: false, forcePasswordChange: false };
    }
  });

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(auth));
  }, [auth]);

  const login = (email, merchant, forcePasswordChange = false) => {
    setAuth({ isLoggedIn: true, userEmail: email, isMerchant: merchant, forcePasswordChange });
  };

  const clearForcePasswordChange = () => {
    setAuth(prev => ({ ...prev, forcePasswordChange: false }));
  };

  const logout = () => {
    setAuth({ isLoggedIn: false, userEmail: null, isMerchant: false, forcePasswordChange: false });
  };

  return (
    <AuthContext.Provider value={{ ...auth, login, logout, clearForcePasswordChange }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
