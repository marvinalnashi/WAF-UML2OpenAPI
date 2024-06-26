/** @type {import('tailwindcss').Config} */
module.exports = {
  darkMode: 'class',
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#FDC300',
        primaryHover: '#1F2937',
        dark: {
          100: '#333333',
          200: '#2C2C2C',
          300: '#262626',
          400: '#1F1F1F',
          500: '#191919',
          600: '#141414',
          700: '#0E0E0E',
          800: '#090909',
          900: '#040404',
        },
      },
    },
  },
  plugins: [],
}
