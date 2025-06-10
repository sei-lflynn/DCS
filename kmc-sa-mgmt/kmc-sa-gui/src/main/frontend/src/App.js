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
 * @author panjames
 */
import React, {useEffect, useState} from "react";
import SaTable from "./sa/SaTable";
import {
    AppBar,
    Box,
    Container,
    createTheme,
    CssBaseline,
    IconButton,
    ThemeProvider,
    Toolbar, Tooltip,
    Typography
} from "@mui/material";
import '@fontsource/roboto/300.css'
import '@fontsource/roboto/400.css'
import '@fontsource/roboto/500.css'
import '@fontsource/roboto/700.css'
import '@fontsource/roboto-mono/300.css'
import '@fontsource/roboto-mono/400.css'
import '@fontsource/roboto-mono/500.css'
import '@fontsource/roboto-mono/700.css'
import {SnackbarProvider} from "notistack";
import {Brightness3, Brightness7, Circle} from "@mui/icons-material";
import {useLocalStorageBool} from "./sa/useLocalStorage";
import ErrorBoundary from "./sa/ErrorBoundary";
import {status} from "./sa/api";

function App() {

    const dark = {
        palette: {
            mode: 'dark',
        },
    }

    const light = {
        palette: {
            mode: 'light'
        }
    }

    const [theme, setTheme] = useLocalStorageBool("theme", true)
    const icon = !theme ? <Brightness3/> : <Brightness7/>
    const appliedTheme = createTheme(theme ? dark : light)
    const [dbStatus, setDbStatus] = useState("error")

    const checkStatus = () => status((r) => {
        setDbStatus("success")
    }, (e) => {
        setDbStatus("error")
    })

    useEffect(() => {
        checkStatus()
        const interval = setInterval(() => {
            checkStatus()
        }, 5000)
        return () => clearInterval(interval)
    }, [])

    return (<React.Fragment>
        <ThemeProvider theme={appliedTheme}>
            <SnackbarProvider maxSnack={5}>
                <CssBaseline/>
                <AppBar position={"static"}>
                    <Container maxWidth={"xl"}>
                        <Toolbar disableGutters>
                            <Typography variant={"h6"} sx={{flexGrow: 1}} noWrap>AMMOS Security Association Database
                                (SADB) Management</Typography>
                            <Box sx={{flexGrow: 0}}>
                                <Tooltip title={'Database is ' + (dbStatus === "success" ? "up" : "down")}><span><Circle
                                    color={dbStatus} sx={{"verticalAlign": "middle"}}/></span></Tooltip>
                                <Tooltip title={'Switch to ' + (theme ? 'light' : 'dark') + ' theme'}><IconButton
                                    onClick={() => setTheme(!theme)}>{icon}</IconButton></Tooltip>
                            </Box>
                        </Toolbar>
                    </Container>
                </AppBar>
                <div className="App">
                    <Container maxWidth={"xl"}>
                        <Box sx={{
                            marginY: 2,
                        }}>
                            <ErrorBoundary>
                                <SaTable/>
                            </ErrorBoundary>
                        </Box>
                    </Container>
                </div>
            </SnackbarProvider>
        </ThemeProvider>
    </React.Fragment>);
}

export default App;
