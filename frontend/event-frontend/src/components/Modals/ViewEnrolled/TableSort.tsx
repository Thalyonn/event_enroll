import { useEffect, useState } from 'react';
import { IconChevronDown, IconChevronUp, IconSearch, IconSelector } from '@tabler/icons-react';
import {
  Center,
  Group,
  keys,
  ScrollArea,
  Table,
  Text,
  TextInput,
  UnstyledButton,
  Button
} from '@mantine/core';
import classes from './TableSort.module.css';
import { useAuth } from '@/context/AuthContext';


interface RowData {
  id: number;
  enrollmentTime: string;
  userId: number;
  email: string;
  username: string;
  eventId: number;
}

interface ThProps {
  children: React.ReactNode;
  reversed: boolean;
  sorted: boolean;
  onSort: () => void;
}

function Th({ children, reversed, sorted, onSort }: ThProps) {
  const Icon = sorted ? (reversed ? IconChevronUp : IconChevronDown) : IconSelector;
  return (
    <Table.Th className={classes.th}>
      <UnstyledButton onClick={onSort} className={classes.control}>
        <Group justify="space-between">
          <Text fw={500} fz="sm">
            {children}
          </Text>
          <Center className={classes.icon}>
            <Icon size={16} stroke={1.5} />
          </Center>
        </Group>
      </UnstyledButton>
    </Table.Th>
  );
}

function filterData(data: RowData[], search: string) {
  const query = search.toLowerCase().trim();
  if (data.length === 0) {
    return [];
  }
  return data.filter((item) =>
    keys(data[0]).some((key) => {
      const val = String(item[key]);
      
      return val.toLowerCase().includes(query)
    })
  );
}

function sortData(
  data: RowData[],
  payload: { sortBy: keyof RowData | null; reversed: boolean; search: string }
) {
  const { sortBy } = payload;

  if (!sortBy) {
    return filterData(data, payload.search);
  }

  return filterData(
    [...data].sort((a, b) => {
      const valB = String(b[sortBy])
      const valA = String(a[sortBy])
      if (payload.reversed) {
        
        return valB.localeCompare(valA);
      }
      
      return valA.localeCompare(valB);
    }),
    payload.search
  );
}

const data_sample = [
  {
    name: 'Athena Weissnat',
    eventName: 'Little - Rippin',
    email: 'Elouise.Prohaska@yahoo.com',
  },
  {
    name: 'Deangelo Runolfsson',
    eventName: 'Greenfelder - Krajcik',
    email: 'Kadin_Trantow87@yahoo.com',
  },
  {
    name: 'Danny Carter',
    eventName: 'Kohler and Sons',
    email: 'Marina3@hotmail.com',
  },
  {
    name: 'Trace Tremblay PhD',
    eventName: 'Crona, Aufderhar and Senger',
    email: 'Antonina.Pouros@yahoo.com',
  },
  {
    name: 'Derek Dibbert',
    eventName: 'Gottlieb LLC',
    email: 'Abagail29@hotmail.com',
  },
  {
    name: 'Viola Bernhard',
    eventName: 'Funk, Rohan and Kreiger',
    email: 'Jamie23@hotmail.com',
  },
  {
    name: 'Austin Jacobi',
    eventName: 'Botsford - Corwin',
    email: 'Genesis42@yahoo.com',
  },
  {
    name: 'Hershel Mosciski',
    eventName: 'Okuneva, Farrell and Kilback',
    email: 'Idella.Stehr28@yahoo.com',
  },
  {
    name: 'Mylene Ebert',
    eventName: 'Kirlin and Sons',
    email: 'Hildegard17@hotmail.com',
  },
  {
    name: 'Lou Trantow',
    eventName: 'Parisian - Lemke',
    email: 'Hillard.Barrows1@hotmail.com',
  },
  {
    name: 'Dariana Weimann',
    eventName: 'Schowalter - Donnelly',
    email: 'Colleen80@gmail.com',
  },
  {
    name: 'Dr. Christy Herman',
    eventName: 'VonRueden - Labadie',
    email: 'Lilyan98@gmail.com',
  },
  {
    name: 'Katelin Schuster',
    eventName: 'Jacobson - Smitham',
    email: 'Erich_Brekke76@gmail.com',
  },
  {
    name: 'Melyna Macejkovic',
    eventName: 'Schuster LLC',
    email: 'Kylee4@yahoo.com',
  },
  {
    name: 'Pinkie Rice',
    eventName: 'Wolf, Trantow and Zulauf',
    email: 'Fiona.Kutch@hotmail.com',
  },
  {
    name: 'Brain Kreiger',
    eventName: 'Lueilwitz Group',
    email: 'Rico98@hotmail.com',
  },
];

interface Enrollment {
    id: number;
    enrollmentTime: string;
    userId: number;
    email: string;
    username: string;
    eventId: number;
}

interface TableSortProps {
    eventId: number;
}

interface EnrollData {
  id: number;
  eventId: number;
  userId: number;
  
}

export function TableSort({eventId} : TableSortProps) {
    const [data, setData] = useState<Enrollment[]>([]);
    const [search, setSearch] = useState('');
    const [sortedData, setSortedData] = useState(data);
    const [sortBy, setSortBy] = useState<keyof RowData | null>(null);
    const [reverseSortDirection, setReverseSortDirection] = useState(false);
    const { isAuthenticated, isAdmin } = useAuth();

    
    const url = `http://localhost:8080/api/enrollments/event/${eventId}`;

    useEffect(() => {
        if(isAuthenticated && isAdmin)
        {
            fetch(url, {credentials: "include"})
            .then((res) => {
            if(res.status === 404) {
                throw new Error("Error 404 on getting enrollments");
            }
            if(!res.ok) {
                throw new Error("Failed to fetch enrollments on event");
            }  
            return res.json();
            })
            .then((data) => setData(data))
            .catch((err) => {console.error('Failed to fetch enrollments', err)
            });
        }
        
    },[]);

    useEffect(() => {
      setSortedData(sortData(data, { sortBy, reversed: reverseSortDirection, search }));
    },[data, sortBy,reverseSortDirection,search]);


    const setSorting = (field: keyof RowData) => {
        const reversed = field === sortBy ? !reverseSortDirection : false;
        setReverseSortDirection(reversed);
        setSortBy(field);
        setSortedData(sortData(data, { sortBy: field, reversed, search }));
    };

    const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const { value } = event.currentTarget;
        setSearch(value);
        setSortedData(sortData(data, { sortBy, reversed: reverseSortDirection, search: value }));
    };

    const handleDelete = async (enrollment : EnrollData) => {
      console.log("enrollment ", enrollment.id, " ", enrollment.eventId," ", enrollment.userId);
      try {
        const res = await fetch(`http://localhost:8080/api/enrollments/admin/${enrollment.eventId}/${enrollment.userId}`,{
          method: 'DELETE',
          credentials: 'include', 
        });
        if(!res.ok) {
          throw new Error("Deleting failed");
        } else if(res.ok) {
          setData(prev => prev.filter(e => !(e.id === enrollment.id)));
        }
      } catch(e) {
        console.error("deleting error: ",  e);
      }
      

    }

    const rows = sortedData.map((row) => (
        <Table.Tr key={row.userId}>
        <Table.Td>{row.username}</Table.Td>
        <Table.Td>{row.email}</Table.Td>
        <Table.Td className={classes.actionColumn}>
            <Button
                size="xs"
                variant="outline"
                onClick={() => {
                  handleDelete({
                    id: row.id,
                    userId: row.userId,
                    eventId: row.eventId
                  })
                }}>
                Delete
            </Button>
        </Table.Td>
        </Table.Tr>
    ));

    return (
        <ScrollArea>
        <TextInput
            placeholder="Search by any field"
            mb="md"
            leftSection={<IconSearch size={16} stroke={1.5} />}
            value={search}
            onChange={handleSearchChange}
        />
        <Table horizontalSpacing="md" verticalSpacing="xs" miw={700} layout="fixed">
            <Table.Tbody>
            <Table.Tr>
                <Th
                sorted={sortBy === 'username'}
                reversed={reverseSortDirection}
                onSort={() => setSorting('username')}
                >
                Name
                </Th>
                <Th
                sorted={sortBy === 'email'}
                reversed={reverseSortDirection}
                onSort={() => setSorting('email')}
                >
                Email
                </Th>
                <Table.Th className={classes.actionColumn}>
                    Actions    
                </Table.Th>
                
            </Table.Tr>
            </Table.Tbody>
            <Table.Tbody>
            {rows.length > 0 ? (
                rows
            ) : (
                <Table.Tr>
                <Table.Td colSpan={data.length > 0 ? Object.keys(data[0]).length : 3}> 
                    <Text fw={500} ta="center">
                    Nothing found
                    </Text>
                </Table.Td>
                </Table.Tr>
            )}
            </Table.Tbody>
        </Table>
        </ScrollArea>
    );
}