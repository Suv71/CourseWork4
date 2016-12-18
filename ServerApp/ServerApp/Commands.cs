using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ServerApp
{
    public static class Commands
    {
        public const int messageToClient = 1;
        public const int newClient = 2;
        public const int clientOut = 3;
        public const int connect = 4;
        public const int disconnect = 5;
        public const int activeClients = 6;
    }
}
