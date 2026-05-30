/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#409EFF',
          dark: '#337ECC',
          light: '#66B1FF',
          lighter: '#79BBFF'
        },
        background: {
          DEFAULT: '#F5F7FA',
          white: '#FFFFFF',
          light: '#F0F2F5'
        },
        text: {
          primary: '#1D2129',
          secondary: '#4E5969',
          placeholder: '#86909C',
          disabled: '#C9CDD4'
        }
      },
      fontFamily: {
        sans: ['Inter', 'PingFang SC', 'Microsoft YaHei', 'sans-serif']
      }
    },
  },
  plugins: [],
}
