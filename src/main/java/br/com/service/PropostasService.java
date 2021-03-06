package br.com.service;

import br.com.business.*;
import br.com.repository.Repository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("proposta")
public class PropostasService {
    private Repository repository = Repository.getINSTANCE();

    @GET
    @Path("all")
    @Produces({MediaType.APPLICATION_JSON})
    public List<Proposta> getAll() {
        return repository.daoPropostas.getAll();
    }

    @GET
    @Path("contratante/{email}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<Proposta> getPropContratante(@PathParam("email") String email) {
        return repository.daoPropostas.getAll().stream().filter(o -> o.getEmailContratante().equals(email)).collect(Collectors.toList());
    }

    @GET
    @Path("artista/{email}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<Proposta> getPropArtista(@PathParam("email") String email) {
        return repository.daoPropostas.getAll().stream().filter(o -> o.getEmailArtista().equals(email)).collect(Collectors.toList());
    }

    @GET
    @Path("evento/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<Proposta> getPropEvento(@PathParam("id") int id) {
        return repository.daoPropostas.getAll().stream().filter(o -> o.getIdEvento() == (id)).collect(Collectors.toList());
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Proposta get(@PathParam("id") int id) {
        return repository.daoPropostas.get(id);
    }

    @POST
    @Path("contratante/add")
    @Consumes({MediaType.APPLICATION_JSON})
    public boolean contratanteAdd(Proposta proposta) {
        Contratante contratante = repository.daoContratantes.get(proposta.getEmailContratante());
        Artista artista = repository.daoArtistas.get(proposta.getEmailArtista());
        Evento evento = repository.daoEventos.get(proposta.getIdEvento());
//        System.out.println(contratante != null && artista != null && evento != null && evento.getEmailContratante().equals(proposta.getEmailContratante()));
        if (contratante != null && artista != null && evento != null && evento.getEmailContratante().equals(proposta.getEmailContratante()) && evento.isOpen()) {
            Proposta propostanew =
                    new Proposta(proposta.getEmailArtista(), proposta.getEmailContratante(), proposta.getIdEvento(), proposta.getValor());
            propostanew.contratanteAceita();
            boolean a = repository.daoPropostas.add(propostanew);
            System.out.println("a = " + a);
            if (!a) {
                Proposta.setMaxId(Proposta.getMaxId() - 1);
                final Proposta[] propostaComp = new Proposta[1];
                repository.daoPropostas.getAll().forEach(o -> {
                    if (o.equals(propostanew)) {
                        propostaComp[0] = o;
                    }
                });
                if (propostaComp[0].isArtistaAceitou()) {
                    propostaComp[0].contratanteAceita();
                    boolean b = repository.daoEventos.addArtistaPendente(proposta.getIdEvento(), proposta.getEmailArtista());
                    boolean c = repository.daoArtistas.addEvento(repository.daoEventos.get(proposta.getIdEvento()), proposta.getEmailArtista());
                    System.out.println("b = " + b);
                    System.out.println("c = " + c);
                    return b && c && repository.daoPropostas.saveInFile();
                }
                return false;
            } else return repository.daoNotificacao.add(new Notificacao(propostanew, Notificacao.ARTISTA));
        } else return false;
    }

    @POST
    @Path("artista/add")
    @Consumes({MediaType.APPLICATION_JSON})
    public boolean artistaAdd(Proposta proposta) {
        Contratante contratante = repository.daoContratantes.get(proposta.getEmailContratante());
        Artista artista = repository.daoArtistas.get(proposta.getEmailArtista());
        Evento evento = repository.daoEventos.get(proposta.getIdEvento());
//        System.out.println(contratante != null && artista != null && evento != null && evento.getEmailContratante().equals(proposta.getEmailContratante()));
        if (contratante != null && artista != null && evento != null && evento.getEmailContratante().equals(proposta.getEmailContratante()) && evento.isOpen()) {
            Proposta propostanew = new Proposta(proposta.getEmailArtista(), proposta.getEmailContratante(), proposta.getIdEvento(), proposta.getValor());
            propostanew.artistaAceita();
            boolean a = repository.daoPropostas.add(propostanew);
            System.out.println("a = " + a);
            if (!a) {
                Proposta.setMaxId(Proposta.getMaxId() - 1);
                final Proposta[] propostaComp = new Proposta[1];
                repository.daoPropostas.getAll().forEach(o -> {
                    if (o.equals(propostanew)) {
                        propostaComp[0] = o;
                    }
                });
                if (propostaComp[0].isContratanteAceitou() && !propostaComp[0].isArtistaAceitou()) {
                    propostaComp[0].artistaAceita();
                    boolean b = repository.daoEventos.addArtistaPendente(proposta.getIdEvento(), proposta.getEmailArtista());
                    boolean c = repository.daoArtistas.addEvento(repository.daoEventos.get(proposta.getIdEvento()), proposta.getEmailArtista());
                    System.out.println("b = " + b);
                    System.out.println("c = " + c);
                    return b && c && repository.daoPropostas.saveInFile();
                }
                return false;
            } else return repository.daoNotificacao.add(new Notificacao(propostanew, Notificacao.CONTRATANTE));
        } else return false;
    }

    @PUT
    @Path("update")
    @Consumes({MediaType.APPLICATION_JSON})
    public boolean update(Proposta proposta) {
        System.out.println(proposta);
        int id = proposta.getId();
        boolean a = repository.daoPropostas.update(id, proposta);
        System.out.println(a);
        return a;
    }

    @DELETE
    @Path("delete/{id}")
    @Produces({MediaType.TEXT_PLAIN})
    public boolean remove(@PathParam("id") int id) {
        System.out.println("DELETE Proposta:");
        List<Proposta> propostas =
                repository.daoPropostas.getAll().stream()
                        .filter(a -> a.getId() == (id))
                        .collect(Collectors.toList());
        if (!propostas.isEmpty()) {
            Proposta proposta = (propostas.get(0));
            System.out.println(proposta);
            if (proposta != null) {
                boolean a = repository.daoPropostas.remove(proposta);
                boolean b = false;
                boolean c = repository.daoArtistas.get(proposta.getEmailArtista()).removeEvento(repository.daoEventos.get(proposta.getIdEvento()));
                boolean d = repository.daoNotificacao.getAll().removeIf(o -> o.getProposta().getIdEvento() == proposta.getIdEvento());
                boolean e = repository.daoNotificacao.saveInFile();
                Set<String> artistasPenDoEvento = repository.daoEventos.get(proposta.getIdEvento()).getEmailArtistasPendente();
                Set<String> artistasConfDoEvento = repository.daoEventos.get(proposta.getIdEvento()).getEmailArtistasConfirmados();
                if (artistasPenDoEvento.contains(proposta.getEmailArtista())) {
                    b = artistasPenDoEvento.removeIf(o -> o.equals(proposta.getEmailArtista()));
                    System.out.println(a);
                    System.out.println(b);
                    System.out.println(c);
                    System.out.println(d);
                    System.out.println(e);
                    return a && b && e && d && c && repository.daoEventos.saveInFile();
                } else if (artistasConfDoEvento.contains(proposta.getEmailArtista())) {
                    b = artistasConfDoEvento.removeIf(o -> o.equals(proposta.getEmailArtista()));
                    System.out.println(a);
                    System.out.println(b);
                    System.out.println(c);
                    System.out.println(d);
                    System.out.println(e);
                    return a && b && e && d && c && repository.daoEventos.saveInFile();
                }
                System.out.println(a);
                System.out.println(c);
                System.out.println(d);
                System.out.println(e);
                return a && c && e && d && repository.daoEventos.saveInFile();
            } else return false;
        } else return false;
    }

//    @GET
//    @Path("attall")
//    @Produces({MediaType.TEXT_PLAIN})
//    public boolean attall() {
//        final boolean[] achou = {false, false};
//        repository.daoPropostas.getAll().stream().forEach(new Consumer<Proposta>() {
//            @Override
//            public void accept(Proposta proposta) {
//                if (proposta.isArtistaAceitou() && proposta.isContratanteAceitou()) {
//                    repository.daoEventos.get(proposta.getIdEvento()).getEmailArtistasPendente().forEach(new Consumer<String>() {
//                        @Override
//                        public void accept(String s) {
//                            if (!achou[0]) {
//                                achou[0] = s.equals(proposta.getEmailArtista());
//                            }
//                        }
//                    });
//                    if (!achou[0]) {
//                        repository.daoEventos.addArtistaPendente(proposta.getIdEvento(), proposta.getEmailArtista());
//                        achou[1] = true;
//                    }
//                }
//            }
//        });
//        return achou[1];
//    }

}
