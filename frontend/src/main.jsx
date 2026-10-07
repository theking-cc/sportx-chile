import React from 'react';
import { createRoot } from 'react-dom/client';
import { HashRouter } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap-icons/font/bootstrap-icons.css';
import './styles.css';
import App from './App';
import { ShopProvider } from './context/ShopContext';
createRoot(document.getElementById('root')).render(<React.StrictMode><HashRouter><ShopProvider><App /></ShopProvider></HashRouter></React.StrictMode>);
