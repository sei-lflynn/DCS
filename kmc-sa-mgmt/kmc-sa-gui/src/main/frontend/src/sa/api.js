/*
 * Copyright 2022, by the California Institute of Technology.
 * ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.
 * Any commercial use must be negotiated with the Office of Technology
 * Transfer at the California Institute of Technology.
 *
 * This software may be subject to U.S. export control laws. By accepting
 * this software, the user agrees to comply with all applicable U.S.
 * export laws and regulations. User has the responsibility to obtain
 * export licenses, or other export authority as may be required before
 * exporting such information to foreign countries or providing access to
 * foreign persons.
 */
/**
 * Backend API calls
 *
 * @author panjames
 */
import axios from "axios";

export const listSa = async (respCallback, errCallback) => {
    try {
        const result = await axios("api/sa")
        respCallback(result)
    } catch (err) {
        errCallback(err)
    }
}

/**
 * Create an SA
 * @param values SA in JSON
 * @param respCallback response callback
 * @param errCallback error callback
 */
export const createSa = (values, respCallback, errCallback) => {
    console.log('creating spi/scid: ' + values.spi + '/' + values.scid)
    axios({
        method: 'put',
        url: "api/sa",
        data: values
    }).then(response => {
        respCallback(response)
    }).catch(e => {
        errCallback(e)
    })
}

/**
 * Start an SA
 * @param id SA ID (SPI/SCID)
 * @param respCallback response callback
 * @param errCallback error callback
 */
export const startSa = (id, respCallback, errCallback) => {
    console.log(`starting ${id.spi}/${id.scid}`)
    axios({
        method: 'post',
        url: 'api/sa/start',
        data: id
    }).then(resp => {
        respCallback(resp)
    }).catch(e => {
        errCallback(e)
    })
}

/**
 * Stop an SA
 * @param id SA ID (SPI/SCID)
 * @param respCallback response callback
 * @param errCallback error callback
 */
export const stopSa = (id, respCallback, errCallback) => {
    console.log(`starting ${id.spi}/${id.scid}`)
    axios({
        method: 'post',
        url: 'api/sa/stop',
        data: id
    }).then(resp => {
        respCallback(resp)
    }).catch(e => {
        errCallback(e)
    })
}

/**
 * Update an SA
 * @param values SA in JSON
 * @param respCallback response callback
 * @param errCallback error callback
 */
export const updateSa = (values, respCallback, errCallback) => {
    console.log('updating spi/scid: ' + values.spi + '/' + values.scid)
    axios({
        method: 'post',
        url: "api/sa",
        data: values
    }).then(resp => {
        respCallback(resp)
    }).catch(e => {
        errCallback(e)
    })
}

/**
 * Delete an SA
 * @param id SA ID (SPI/SCID)
 * @param respCallback response callback
 * @param errCallback error callback
 */
export const deleteSa = (id, respCallback, errCallback) => {
    console.log('deleting spi/scid: ' + id.spi + '/' + id.scid)
    axios({
        method: 'delete',
        url: "api/sa",
        data: [id]
    }).then(resp => {
        respCallback(resp)
    }).catch(e => {
        errCallback(e)
    })
}

/**
 * Delete multiple SAs
 * @param ids array of SA IDs (SPI/SCID)
 * @param respCallback
 * @param errCallback
 */
export const deleteSas = (ids, respCallback, errCallback) => {
    console.log('deleting multiple SAs')
    axios({
        method: 'delete',
        url: "api/sa",
        data: ids
    }).then(resp => {
        respCallback(resp)
    }).catch(e => {
        errCallback(e)
    })
}

/**
 * Expire an SA
 * @param id SA ID (SPI/SCID)
 * @param respCallback response callback
 * @param errCallback error callback
 */
export const expireSa = (id, respCallback, errCallback) => {
    console.log('expiring spi/scid: ' + id.spi + '/' + id.scid)
    axios({
        method: 'post',
        url: "api/sa/expire",
        data: id
    }).then(resp => {
        respCallback(resp)
    }).catch(e => {
        errCallback(e)
    })
}

/**
 * Reset ARSN
 * @param id
 * @param arsn
 * @param respCallback
 * @param errCallback
 */
export const resetArsn = (id, arsn, respCallback, errCallback) => {
    console.log(`resetting arsn to ${arsn} on spi/scid: ${id.spi} / ${id.scid}`)
    axios({
        method: 'post',
        url: "api/sa/arsn",
        data: {
            id: id,
            arsn: arsn
        }
    }).then(resp => {
        respCallback(resp)
    }).catch(e => {
        errCallback(e)
    })
}

/**
 * Create SAs via CSV upload
 * @param data form data
 * @param setProgress progress function
 * @param respCallback response callback
 * @param errCallback error callback
 */
export const bulkCreate = (data, setProgress, respCallback, errCallback) => {
    console.log(`creating multiple SAs via CSV upload`)
    axios({
        method: 'post',
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        url: 'api/sa/create',
        data: data,
        onUploadProgress: (progressEvent) => {
            const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
            setProgress(progress)
        }
    }).then(r => {
        respCallback(r)
    }).catch(e => {
        errCallback(e)
    })
}

/**
 * Dowload SADB CSV
 * @param respCallback response callback
 * @param errCallback error callback
 */
export const downloadCsv = (respCallback, errCallback) => {
    console.log('downloading sadb csv')
    axios({
        method: 'get',
        url: 'api/sa/csv',
        responseType: 'blob'
    }).then(r => {
        respCallback(r)
    }).catch(e => {
        errCallback(e)
    })
}

/**
 * Create a response callback
 * @param msg snackbar message
 * @param onClose close action
 * @param refresh refresh table
 * @param enqueueSnackbar snackbar queue
 * @returns {(function(*): void)|*} response callback
 */
export const createResponseCallback = (msg, onClose, refresh, enqueueSnackbar) => {
    return (resp) => {
        if (resp.status >= 200 && resp.status < 300) {
            onClose()
            refresh()
            enqueueSnackbar(msg, {variant: "success"})
        }
    }
}

/**
 * Create an error callback
 * @param enqueueSnackbar snackbar queue
 * @returns {(function(*): void)|*} error callback
 */
export const createErrorCallback = (enqueueSnackbar) => {
    return (e) => {
        let txt
        if (e.response) {
            txt = `HTTP ${e.response.status}:`
            if (e.response.data.messages.length > 1) {
                txt += `${e.response.data.messages.length} error messages received, see console for details`
            } else {
                e.response.data.messages.forEach(m => txt += m)
            }
        } else if (e.request) {
            txt = `HTTP ${e.request.status}: Request failed`
        } else {
            txt = `${e.message}`
        }
        console.log(e)
        console.log(txt)
        console.log(e.response.data.messages)
        enqueueSnackbar(txt, {variant: "error"})
    }
}

/**
 * Modify ARSN
 * @param data spi/scid id, arsn, arsnLen, arswn
 * @param respCallback response callback
 * @param errCallback error callback
 */
export const modifyArsn = (data, respCallback, errCallback) => {
    axios({
        method: 'post',
        url: 'api/sa/arsn',
        data: data
    }).then((r) => {
        respCallback(r)
    }).catch((e) => {
        errCallback(e)
    })
}

/**
 * Modify IV
 * @param data spi/scid, iv, ivLen
 * @param respCallback response callback
 * @param errCallback error callback
 */
export const modifyIv = (data, respCallback, errCallback) => {
    axios({
        method: 'post',
        url: 'api/sa/iv',
        data: data
    }).then((r) => {
        respCallback(r)
    }).catch((e) => {
        errCallback(e)
    })
}

/**
 * Rekey SA
 * @param data spi/scid id, ekid, akid
 * @param respCallback response callback
 * @param errCallback error callback
 */
export const rekeySa = (data, respCallback, errCallback) => {
    axios({
        method: 'post',
        url: 'api/sa/key',
        data: data
    }).then(r => {
        respCallback(r)
    }).catch(e => {
        errCallback(e)
    })
}

export const status = (respCallback, errCallback) => {
    axios({
        method: 'get',
        url: 'api/status'
    }).then(r => {
        respCallback(r)
    }).catch(e => {
        errCallback(e)
    })
}