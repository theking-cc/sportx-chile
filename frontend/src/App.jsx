import { Routes, Route, Link } from 'react-router-dom';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import Catalog from './pages/Catalog';
import Detail from './pages/Detail';
import Cart from './pages/Cart';
import { useShop } from './context/ShopContext';
function Pending({title}) { return <><h1>{title}</h1><p>Esta sección se migrará en la siguiente etapa.</p><Link to="/">Volver al inicio</Link></>; }
export default function App() { const {message,dismiss} = useShop(); return <div className="d-flex flex-column min-vh-100"><Navbar/><main className="container py-4 py-md-5 flex-grow-1">{message && <div role="status" className="alert alert-info d-flex justify-content-between align-items-center">{message}<button className="btn-close" aria-label="Cerrar mensaje" onClick={dismiss}/></div>}<Routes><Route path="/" element={<Home/>}/><Route path="/productos" element={<Catalog/>}/><Route path="/productos/:id" element={<Detail/>}/><Route path="/carrito" element={<Cart/>}/>{[['nosotros','Nosotros'],['blogs','Blogs'],['contacto','Contacto'],['login','Iniciar sesión'],['registro','Registro'],['admin','Administración']].map(([path,title]) => <Route key={path} path={'/' + path} element={<Pending title={title}/>}/>)}<Route path="*" element={<Pending title="Página no encontrada"/>}/></Routes></main><footer className="bg-dark text-white py-4 text-center">Sport<span className="text-danger">X</span> Chile · Equipamiento deportivo</footer></div>; }
