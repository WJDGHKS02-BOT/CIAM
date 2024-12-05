/** @type {import('tailwindcss').Config} */

export default {
  content: ['./src/**/*.{js,jsx,ts,tsx,html}'],
  theme: {
    extend: {
      colors: {
        primary: {
          default: '#0068ea',
          dark: '#0654b6',
          light: '#f5f7fe',
        },
        error: {
          default: '#e03030',
          dark: '#b92525',
          light: '#f5f0f0',
        },
        gray: {
          default: '#000000',
          '01': '#313131',
          '02': '#757575',
          '03': '#8f8f8f',
          '04': '#bbbbbb',
          '05': '#dddddd',
        },
        white: '#ffffff',
      },
      fontSize: {
        'samsung-800-40': ['40px', { lineHeight: '48px', fontWeight: '800' }],
        'samsung-800-24': ['24px', { lineHeight: '32px', fontWeight: '800' }],
        'samsung-800-18': ['18px', { lineHeight: '24px', fontWeight: '800' }],
        'samsung-700-32': ['32px', { lineHeight: '40px', fontWeight: '700' }],
        'samsung-700-24': ['24px', { lineHeight: '32px', fontWeight: '700' }],
        'samsung-700-20': ['20px', { lineHeight: '28px', fontWeight: '700' }],
        'samsung-700-18': ['18px', { lineHeight: '24px', fontWeight: '700' }],
        'samsung-700-16': ['16px', { lineHeight: '24px', fontWeight: '700' }],
        'samsung-700-14': ['14px', { lineHeight: '20px', fontWeight: '700' }],
        'samsung-700-12': ['12px', { lineHeight: '18px', fontWeight: '700' }],
        'samsung-400-16': ['16px', { lineHeight: '24px', fontWeight: '400' }],
        'samsung-400-14': ['14px', { lineHeight: '20px', fontWeight: '400' }],
        'samsung-400-12': ['12px', { lineHeight: '18px', fontWeight: '400' }],
        'samsung-200-12': ['12px', { lineHeight: '18px', fontWeight: '200' }],
      },
    },
  },
  plugins: [
    function ({ addBase }) {
      addBase({
        html: {
          fontFamily: 'SamsungOne, sans-serif',
        },
      });
    },
  ],
};
