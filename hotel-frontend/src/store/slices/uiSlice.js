import { createSlice } from '@reduxjs/toolkit'

const uiSlice = createSlice({
  name: 'ui',
  initialState: {
    sidebarOpen:   true,
    theme:         'light',
    notifications: [],
  },
  reducers: {
    toggleSidebar:    (s) => { s.sidebarOpen = !s.sidebarOpen },
    setSidebarOpen:   (s, { payload }) => { s.sidebarOpen = payload },
    setTheme:         (s, { payload }) => { s.theme = payload },
    addNotification:  (s, { payload }) => { s.notifications.unshift({ id: Date.now(), ...payload }) },
    removeNotification: (s, { payload: id }) => {
      s.notifications = s.notifications.filter(n => n.id !== id)
    },
  },
})

export const { toggleSidebar, setSidebarOpen, setTheme, addNotification, removeNotification } = uiSlice.actions
export default uiSlice.reducer

export const selectSidebarOpen   = (s) => s.ui.sidebarOpen
export const selectNotifications = (s) => s.ui.notifications
export const selectTheme         = (s) => s.ui.theme
