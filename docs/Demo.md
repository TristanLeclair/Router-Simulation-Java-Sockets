- [ ] Window 1
`attach 127.0.0.1 5002 192.168.1.100`

- [ ] Window 3
`attach 127.0.0.1 5002 192.168.1.100`

- [ ] Window 2
`start`

- [ ] Window 1
`start`

- [ ] Window 3
`start`

- [ ] Window 4
`attach 127.0.0.1 5002 192.168.1.100`
`attach 127.0.0.1 5003 192.168.2.1`

- [ ] Window 1
`detect 192.168.3.1`

- [ ] Window 2
`disconnect 2`

- [ ] Window 1
`detect 192.168.3.1`

- [ ] Window 5
`connect 127.0.0.1 5003 192.168.2.1`

- [ ] Window 1
`detect 192.168.4.1`

- [ ] Window 5
`quit`

- [ ] Window 1
`detect 192.168.4.1`