import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { billingApi } from '../../api/billingApi'
import toast from 'react-hot-toast'

export const fetchInvoices = createAsyncThunk('billing/fetchInvoices',
  async (params, { rejectWithValue }) => {
    try { return (await billingApi.getInvoices(params)).data }
    catch (e) { return rejectWithValue(e.response?.data?.detail) }
  })

export const createInvoice = createAsyncThunk('billing/createInvoice',
  async (reservationId, { rejectWithValue }) => {
    try { return (await billingApi.createInvoice(reservationId)).data }
    catch (e) { return rejectWithValue(e.response?.data?.detail) }
  })

export const processPayment = createAsyncThunk('billing/processPayment',
  async ({ data, idempotencyKey }, { rejectWithValue }) => {
    try { return (await billingApi.processPayment(data, idempotencyKey)).data }
    catch (e) { return rejectWithValue(e.response?.data?.detail || 'Payment failed') }
  })

const billingSlice = createSlice({
  name: 'billing',
  initialState: {
    invoices:        { content: [], totalElements: 0, totalPages: 0 },
    selectedInvoice: null,
    loading:         false,
    paymentLoading:  false,
    error:           null,
  },
  reducers: {
    setSelectedInvoice: (s, { payload }) => { s.selectedInvoice = payload },
  },
  extraReducers: (b) => {
    b
      .addCase(fetchInvoices.pending,   (s) => { s.loading = true })
      .addCase(fetchInvoices.fulfilled, (s, { payload }) => { s.loading = false; s.invoices = payload })
      .addCase(fetchInvoices.rejected,  (s, { payload }) => { s.loading = false; s.error = payload })
      .addCase(createInvoice.fulfilled, (s, { payload }) => {
        s.invoices.content.unshift(payload)
        toast.success('Invoice created')
      })
      .addCase(processPayment.pending,   (s) => { s.paymentLoading = true })
      .addCase(processPayment.fulfilled, (s) => { s.paymentLoading = false; toast.success('Payment processed') })
      .addCase(processPayment.rejected,  (s, { payload }) => { s.paymentLoading = false; toast.error(payload) })
  },
})

export const { setSelectedInvoice } = billingSlice.actions
export default billingSlice.reducer

export const selectInvoices        = (s) => s.billing.invoices
export const selectSelectedInvoice = (s) => s.billing.selectedInvoice
export const selectBillingLoading  = (s) => s.billing.loading
