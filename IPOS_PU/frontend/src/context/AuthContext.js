import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

const STORAGE_KEY = 'ipos_auth';

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      return stored ? JSON.parse(stored) : { isLoggedIn: false, userEmail: null, isMerchant: false };
    } catch {
      return { isLoggedIn: false, userEmail: null, isMerchant: false };
    }
  });

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(auth));
  }, [auth]);

  const login = (email, merchant) => {
    setAuth({ isLoggedIn: true, userEmail: email, isMerchant: merchant });
  };

  const logout = () => {
    setAuth({ isLoggedIn: false, userEmail: null, isMerchant: false });
  };

  return (
    <AuthContext.Provider value={{ ...auth, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
