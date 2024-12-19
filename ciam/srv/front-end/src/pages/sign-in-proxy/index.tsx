import { createRoot } from 'react-dom/client';

import SignInProxy from './sign-in-proxy';

const root = createRoot(document.getElementById('page-root')!);
root.render(<SignInProxy />);
