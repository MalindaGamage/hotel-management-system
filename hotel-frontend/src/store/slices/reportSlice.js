import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { reportApi } from '../../api/reportApi'

export const fetchKpiSummary = createAsyncThunk('reports/fetchKpis',
  async (params, { rejectWithValue }) => {
    try { return (await reportApi.getDashboard(params)).data }
    catch (e) { return rejectWithValue(e.response?.data?.detail) }
  })

const reportSlice = createSlice({
  name: 'reports',
  initialState: {
    kpis:     null,
    loading:  false,
    error:    null,
    dateRange: { start: null, end: null },
  },
  reducers: {
    setDateRange: (s, { payload }) => { s.dateRange = payload },
  },
  extraReducers: (b) => {
    b
      .addCase(fetchKpiSummary.pending,   (s) => { s.loading = true })
      .addCase(fetchKpiSummary.fulfilled, (s, { payload }) => { s.loading = false; s.kpis = payload })
      .addCase(fetchKpiSummary.rejected,  (s, { payload }) => { s.loading = false; s.error = payload })
  },
})

export const { setDateRange } = reportSlice.actions
export default reportSlice.reducer

export const selectKpis         = (s) => s.reports.kpis
export const selectReportsLoading = (s) => s.reports.loading
